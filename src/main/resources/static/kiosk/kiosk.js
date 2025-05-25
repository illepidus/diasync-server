const centerTextPlugin = {
  id: 'centerText',
  afterDatasetsDraw(chart) {
    const { ctx, width, height } = chart;
    const text = chart.options.plugins.centerText?.text;
    const color = chart.options.plugins.centerText?.color || '#fff';
    if (!text) return;
    ctx.save();
    ctx.font = 'bold 48vh Audiowide, sans-serif';
    ctx.textAlign = 'center';
    ctx.textBaseline = 'middle';
    ctx.fillStyle = color;
    ctx.lineWidth = 10;
    ctx.strokeStyle = '#000';
    ctx.strokeText(text, width / 2, height / 2);
    ctx.fillText(text, width / 2, height / 2);
    ctx.restore();
  }
};

const params    = new URLSearchParams(location.search);
const DEFAULTS = { period: '1h', low: 70, high: 180, stale: 10, baseRadius: 6 };
const USER_ID   = params.get('userId') || 'demo';
const UNIT      = params.get('unit') === 'mgdl' ? 'mgdl' : 'mmol';
const USE_CALIB = params.get('calibrations') !== 'false';
const LOW       = parseFloat(params.get('low'))  || DEFAULTS.low;
const HIGH      = parseFloat(params.get('high')) || DEFAULTS.high;
const STALE_MIN = parseFloat(params.get('stale')) || DEFAULTS.stale;
const PERIOD_MS = (() => {
  const m = /^(\d+)([smhd])$/.exec(params.get('period') || DEFAULTS.period);
  const mult = { s: 1e3, m: 6e4, h: 36e5, d: 864e5 };
  return m ? +m[1] * (mult[m[2]] || 36e5) : 36e5;
})();

const ctx = document.getElementById('bg').getContext('2d');
Chart.register(Chart.registry.getPlugin('annotation'), centerTextPlugin);

const dataPoints = [];
let lastTimestamp = Date.now();

function applyCalib(mgdl, cal) {
  return USE_CALIB && cal ? mgdl * cal.slope + cal.intercept : mgdl;
}

function toDisplay(mgdl, cal) {
  const v = applyCalib(mgdl, cal);
  return UNIT === 'mgdl' ? Math.round(v).toString() : (v / 18).toFixed(1);
}

function getColor(mgdl) {
  return mgdl < LOW ? 'red' : mgdl > HIGH ? 'orange' : 'white';
}

let chart;
function initChart() {
  const radius = Math.max(2, Math.round(DEFAULTS.baseRadius * 3600000 / PERIOD_MS));
  chart = new Chart(ctx, {
    type: 'scatter',
    data: { datasets: [{ data: [], pointRadius: radius, pointHoverRadius: radius * 1.5, backgroundColor: [] }] },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      animation: false,
      interaction: { mode: 'nearest', intersect: true },
      scales: {
        x: { display: false, type: 'time', min: () => Date.now() - PERIOD_MS, max: () => Date.now() },
        y: { display: false }
      },
      plugins: {
        legend: { display: false },
        tooltip: {
          callbacks: {
            title: items => new Date(items[0].raw.x).toLocaleTimeString('en-GB', {
              hour12: false, hour: '2-digit', minute: '2-digit', second: '2-digit'
            }),
            label: ctx => {
              const d = ctx.raw;
              return [
                `Value: ${toDisplay(d.mgdl, d.calibration)} ${UNIT}`,
                `Sensor: ${d.sensorId}`,
                `Slope: ${d.calibration?.slope?.toFixed(3) || '-'}`,
                `Intercept: ${d.calibration?.intercept?.toFixed(3) || '-'}`
              ];
            }
          }
        },
        annotation: {
          annotations: {
            lowLine:  { type: 'line', yMin: LOW, yMax: LOW, borderColor: 'red', borderWidth: 2 },
            highLine: { type: 'line', yMin: HIGH, yMax: HIGH, borderColor: 'orange', borderWidth: 2 }
          }
        },
        centerText: { text: '--', color: 'white' }
      }
    }
  });
}

function updateDisplay(mgdl, cal) {
  chart.options.plugins.centerText.text  = toDisplay(mgdl, cal);
  chart.options.plugins.centerText.color = getColor(mgdl);
  lastTimestamp = Date.now();
}

function updateChart() {
  const now = Date.now();
  chart.options.scales.x.min = now - PERIOD_MS;
  chart.options.scales.x.max = now;

  const recent = dataPoints.filter(p => new Date(p.x).getTime() >= now - PERIOD_MS);
  chart.data.datasets[0].data = recent;
  chart.data.datasets[0].backgroundColor = recent.map(p => p.backgroundColor);

  if (recent.length) {
    const ys = recent.map(p => p.mgdl);
    const delta = 18;
    chart.options.scales.y.min = Math.min(...ys, LOW) - delta;
    chart.options.scales.y.max = Math.max(...ys, HIGH) + delta;
  }

  if ((now - lastTimestamp) / 60000 > STALE_MIN) {
    chart.options.plugins.centerText.text = '???';
    chart.options.plugins.centerText.color = 'purple';
  }

  chart.update('none');
}

async function loadInitial() {
  const to   = new Date().toISOString();
  const from = new Date(Date.now() - PERIOD_MS).toISOString();
  const query = `query($uid:String!,$from:String!,$to:String!){
    getDataPoints(userId:$uid,from:$from,to:$to){
      timestamp sensorGlucose{mgdl sensorId calibration{slope intercept}}
    }}`;
  const res = await fetch('/graphql', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ query, variables: { uid: USER_ID, from, to } })
  });
  const pts = (await res.json()).data.getDataPoints || [];
  pts.forEach(pt => {
    const sg = pt.sensorGlucose;
    dataPoints.push({
      x: pt.timestamp,
      y: sg.mgdl,
      mgdl: sg.mgdl,
      sensorId: sg.sensorId,
      calibration: sg.calibration,
      backgroundColor: getColor(sg.mgdl)
    });
  });
  if (pts.length) {
    const last = pts[pts.length - 1].sensorGlucose;
    updateDisplay(last.mgdl, last.calibration);
  }
}

function startSubscription() {
  const client = graphqlWs.createClient({
    url: `${location.protocol === 'https:' ? 'wss' : 'ws'}://${location.host}/graphql`,
    retryAttempts: Infinity,
    shouldRetry: () => true
  });
  const subQ = `subscription{onDataPointAdded(userId:"${USER_ID}"){
    timestamp sensorGlucose{mgdl sensorId calibration{slope intercept}}}}`;
  client.subscribe({ query: subQ }, {
    next({ data }) {
      const sg = data.onDataPointAdded.sensorGlucose;
      dataPoints.push({
        x: data.onDataPointAdded.timestamp,
        y: sg.mgdl,
        mgdl: sg.mgdl,
        sensorId: sg.sensorId,
        calibration: sg.calibration,
        backgroundColor: getColor(sg.mgdl)
      });
      updateDisplay(sg.mgdl, sg.calibration);
    },
    error: () => setTimeout(startSubscription, 3000),
    complete: () => setTimeout(startSubscription, 3000)
  });
}

initChart();
loadInitial().catch(err => console.error('Failed to load data:', err));
setInterval(updateChart, 1000);
startSubscription();
