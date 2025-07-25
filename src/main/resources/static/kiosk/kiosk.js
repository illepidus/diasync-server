const params = new URLSearchParams(location.search);
const DEFAULTS = {period: '1h', low: 70, high: 180, stale: 10, baseRadius: 6};
const USER_ID = params.get('userId') || 'demo';
const UNIT = params.get('unit') === 'mgdl' ? 'mgdl' : 'mmol';
const USE_CALIB = params.get('calibrations') !== 'false';
const LOW = parseFloat(params.get('low')) || DEFAULTS.low;
const HIGH = parseFloat(params.get('high')) || DEFAULTS.high;
const STALE_MIN = parseFloat(params.get('stale')) || DEFAULTS.stale;
const PERIOD_MS = (() => {
    const m = /^(\d+)([smhd])$/.exec(params.get('period') || DEFAULTS.period);
    const mult = {s: 1e3, m: 6e4, h: 36e5, d: 864e5};
    return m ? +m[1] * (mult[m[2]] || 36e5) : 36e5;
})();

const COLORS = {
    stale: '#ff00ff',
    offline: '#ff0030',
    low: '#ff0030',
    high: '#ffc000',
    normal: '#ffffff',
    manual: '#ff0030',
    stroke: '#000000'
};

const centerTextPlugin = {
    id: 'centerText',
    afterDatasetsDraw(chart) {
        const {ctx, width, height} = chart;
        const text = chart.options.plugins.centerText?.text;
        const color = chart.options.plugins.centerText?.color || COLORS.stale;
        if (!text) return;

        ctx.save();
        ctx.textAlign = 'center';
        ctx.textBaseline = 'middle';
        ctx.fillStyle = color;
        ctx.strokeStyle = COLORS.stroke;

        const fontFamily = 'Chakra Petch, sans-serif';
        let fontSize = height * 0.8;
        ctx.font = `bold ${fontSize}px ${fontFamily}`;

        let textMetrics = ctx.measureText(text);
        while ((textMetrics.width > width * 0.9 || fontSize > height * 0.8) && fontSize > 10) {
            fontSize -= 2;
            ctx.font = `bold ${fontSize}px ${fontFamily}`;
            textMetrics = ctx.measureText(text);
        }

        const offsetY = (textMetrics.actualBoundingBoxAscent - textMetrics.actualBoundingBoxDescent) / 2;
        const centerX = width / 2;
        const centerY = height / 2 + offsetY;

        ctx.lineWidth = fontSize * 0.025;
        ctx.strokeText(text, centerX, centerY);
        ctx.fillText(text, centerX, centerY);
        ctx.restore();
    }
};

const offlineStatusPlugin = {
    id: 'offlineStatus',
    afterDatasetsDraw(chart) {
        if (!chart.options.plugins.offlineStatus?.enabled) return;

        const {ctx, width, height} = chart;
        const text = 'OFFLINE';
        const color = COLORS.offline;
        const strokeColor = COLORS.stroke;
        const fontFamily = 'Chakra Petch, sans-serif';

        let fontSize = height * 0.1;
        ctx.font = `bold ${fontSize}px ${fontFamily}`;

        while (ctx.measureText(text).width > width * 0.95 && fontSize > 10) {
            fontSize -= 2;
            ctx.font = `bold ${fontSize}px ${fontFamily}`;
        }

        ctx.save();
        ctx.textAlign = 'center';
        ctx.textBaseline = 'top';
        ctx.fillStyle = color;
        ctx.strokeStyle = strokeColor;
        ctx.lineWidth = fontSize * 0.025;

        const centerX = width / 2;
        const offsetY = height * 0.025;

        ctx.strokeText(text, centerX, offsetY);
        ctx.fillText(text, centerX, offsetY);
        ctx.restore();
    }
};

const ctx = document.getElementById('bg').getContext('2d');
Chart.register(Chart.registry.getPlugin('annotation'), centerTextPlugin);
Chart.register(offlineStatusPlugin);

const sensorPoints = [];
const manualPoints = [];
let lastTimestamp = Date.now();
let isWebSocketConnected = false;

function applyCalib(mgdl, cal) {
    return USE_CALIB && cal ? mgdl * cal.slope + cal.intercept : mgdl;
}

function toDisplay(mgdl, cal) {
    const v = applyCalib(mgdl, cal);
    return UNIT === 'mgdl' ? Math.round(v).toString() : (v / 18).toFixed(1);
}

function getColor(mgdl) {
    return mgdl < LOW ? COLORS.low : mgdl > HIGH ? COLORS.high : COLORS.normal;
}

let chart;

function initChart() {
    const radius = Math.max(2, Math.round(DEFAULTS.baseRadius * 3600000 / PERIOD_MS));
    chart = new Chart(ctx, {
        type: 'scatter',
        data: {
            datasets: [
                {
                    label: 'Sensor',
                    data: [],
                    pointRadius: radius,
                    pointHoverRadius: radius * 1.5,
                    pointStyle: 'circle',
                    backgroundColor: []
                },
                {
                    label: 'Manual',
                    data: [],
                    pointRadius: radius * 1.5,
                    pointHoverRadius: radius * 2,
                    pointStyle: 'rect',
                    backgroundColor: COLORS.manual,
                    borderColor: COLORS.normal,
                    borderWidth: radius / 2
                }
            ]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            animation: false,
            interaction: {mode: 'nearest', intersect: true},
            scales: {
                x: {display: false, type: 'time', min: () => Date.now() - PERIOD_MS, max: () => Date.now()},
                y: {display: false}
            },
            plugins: {
                legend: {display: false},
                tooltip: {
                    callbacks: {
                        title: items => new Date(items[0].raw.x).toLocaleTimeString('en-GB', {
                            hour12: false, hour: '2-digit', minute: '2-digit', second: '2-digit'
                        }),
                        label: ctx => {
                            const d = ctx.raw;
                            if (d.mgdl !== undefined) {
                                return [
                                    `Value: ${toDisplay(d.mgdl, d.calibration)} ${UNIT}`,
                                    `Sensor: ${d.sensorId}`,
                                    `Slope: ${d.calibration?.slope?.toFixed(3) || '-'}`,
                                    `Intercept: ${d.calibration?.intercept?.toFixed(3) || '-'}`
                                ];
                            } else {
                                return [`Manual: ${UNIT === 'mgdl' ? Math.round(d.y) : (d.y / 18).toFixed(1)} ${UNIT}`];
                            }
                        }
                    }
                },
                annotation: {
                    drawTime: 'beforeDatasetsDraw',
                    annotations: {
                        lowLine: {
                            display: false,
                            type: 'line',
                            yMin: LOW,
                            yMax: LOW,
                            borderColor: COLORS.low,
                            borderWidth: 2,
                            z: -10
                        },
                        highLine: {
                            display: false,
                            type: 'line',
                            yMin: HIGH,
                            yMax: HIGH,
                            borderColor: COLORS.high,
                            borderWidth: 2,
                            z: -10
                        }
                    }
                },
                centerText: {text: '???', color: COLORS.stale},
                offlineStatus: {enabled: false}
            }
        }
    });
}

function updateDisplay(mgdl, cal) {
    const calibrated = applyCalib(mgdl, cal);
    chart.options.plugins.centerText.text = toDisplay(mgdl, cal);
    chart.options.plugins.centerText.color = getColor(calibrated);
    lastTimestamp = Date.now();
}

function updateChart() {
    const now = Date.now();
    chart.options.scales.x.min = now - PERIOD_MS;
    chart.options.scales.x.max = now;

    const recentSensor = sensorPoints.filter(p => new Date(p.x).getTime() >= now - PERIOD_MS);
    const recentManual = manualPoints.filter(p => new Date(p.x).getTime() >= now - PERIOD_MS);

    chart.data.datasets[0].data = recentSensor;
    chart.data.datasets[0].backgroundColor = recentSensor.map(p => p.backgroundColor);
    chart.data.datasets[1].data = recentManual;

    if (recentSensor.length || recentManual.length) {
        const allY = [...recentSensor.map(p => p.mgdl), ...recentManual.map(p => p.y)];
        const delta = 18;
        chart.options.scales.y.min = Math.min(...allY, LOW) - delta;
        chart.options.scales.y.max = Math.max(...allY, HIGH) + delta;
    }

    if ((now - lastTimestamp) / 60000 > STALE_MIN) {
        chart.options.plugins.centerText.text = '???';
        chart.options.plugins.centerText.color = COLORS.stale;
    }

    const showLines = recentSensor.length > 0 || recentManual.length > 0;
    chart.options.plugins.annotation.annotations.lowLine.display = showLines;
    chart.options.plugins.annotation.annotations.highLine.display = showLines;

    chart.update('none');
}

function pushSensorPoint(ts, sg) {
    const calibrated = applyCalib(sg.mgdl, sg.calibration);
    sensorPoints.push({
        x: ts,
        y: calibrated,
        mgdl: sg.mgdl,
        sensorId: sg.sensorId,
        calibration: sg.calibration,
        backgroundColor: getColor(calibrated)
    });
}

function markDisconnected() {
    if (!isWebSocketConnected) return;
    isWebSocketConnected = false;
    chart.options.plugins.offlineStatus.enabled = true;
    chart.update('none');
}

function markConnected() {
    if (isWebSocketConnected) return;
    isWebSocketConnected = true;
    chart.options.plugins.offlineStatus.enabled = false;
    chart.update('none');
}

function loadInitial() {
    const to = new Date().toISOString();
    const from = new Date(Date.now() - PERIOD_MS).toISOString();
    const query = `
        query ($uid: String!, $from: String!, $to: String!) {
          getDataPoints(userId: $uid, from: $from, to: $to) {
            timestamp
            sensorGlucose {
              mgdl
              sensorId
              calibration {
                slope
                intercept
              }
            }
            manualGlucose {
              mgdl
            }
          }
        }`;

    fetch('/graphql', {
        method: 'POST',
        headers: {'Content-Type': 'application/json'},
        body: JSON.stringify({query, variables: {uid: USER_ID, from, to}})
    })
        .then(res => res.json())
        .then(json => {
            const pts = json.data.getDataPoints || [];
            pts.forEach(pt => {
                const ts = pt.timestamp;
                const sg = pt.sensorGlucose;
                const mg = pt.manualGlucose;

                if (sg && sg.mgdl != null) {
                    pushSensorPoint(ts, sg);
                }
                if (mg && mg.mgdl != null) {
                    manualPoints.push({x: ts, y: mg.mgdl});
                }
            });

            const last = pts.at(-1)?.sensorGlucose;
            if (last?.mgdl != null) {
                updateDisplay(last.mgdl, last.calibration);
            }
        })
        .catch(err => console.error('Failed to load data:', err));
}

function startSubscription() {
    const client = graphqlWs.createClient({
        url: `${location.protocol === 'https:' ? 'wss' : 'ws'}://${location.host}/graphql`,
        retryAttempts: Infinity,
        shouldRetry: () => true,
        on: {
            connected: () => {
                markConnected();
            },
            closed: () => {
                markDisconnected();
            }
        }
    });

    const subscriptionQuery = `
    subscription {
      onDataPointAdded(userId: "${USER_ID}") {
        timestamp
        sensorGlucose {
          mgdl
          sensorId
          calibration {
            slope
            intercept
          }
        }
        manualGlucose {
          mgdl
        }
      }
    }`;

    client.subscribe({query: subscriptionQuery}, {
        next({data}) {
            const ts = data.onDataPointAdded.timestamp;
            const sg = data.onDataPointAdded.sensorGlucose;
            const mg = data.onDataPointAdded.manualGlucose;

            if (sg && sg.mgdl != null) {
                pushSensorPoint(ts, sg);
                updateDisplay(sg.mgdl, sg.calibration);
            }
            if (mg && mg.mgdl != null) {
                manualPoints.push({x: ts, y: mg.mgdl});
            }
        },
        error: () => setTimeout(startSubscription, 3000),
        complete: () => setTimeout(startSubscription, 3000)
    });
}

initChart();
loadInitial();
setInterval(updateChart, 500);
startSubscription();
