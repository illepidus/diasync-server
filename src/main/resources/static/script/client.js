const userId = "demo";
const graphqlEndpoint = `${window.location.protocol}//${window.location.host}/graphql`;
const wsEndpoint = `ws${window.location.protocol === 'https:' ? 's' : ''}://${window.location.host}/graphql`;
const dataPoints = [];

const ctx = document.getElementById('glucoseChart').getContext('2d');
const chart = new Chart(ctx, {
    type: 'scatter',
    data: {
        datasets: [{
            label: 'Blood Glucose (mg/dL)',
            data: dataPoints,
            borderColor: '#10a37f',
            backgroundColor: 'rgba(16, 163, 127, 0.2)',
            fill: true,
            tension: 0.5
        }]
    },
    options: {
        plugins: {
            legend: {labels: {color: '#ececf1'}},
            tooltip: {
                callbacks: {
                    label: function (context) {
                        let value = context.raw.y.toFixed(1);
                        return `Glucose: ${value} mg/dL`;
                    },
                    title: function (context) {
                        let date = new Date(context[0].raw.x);
                        return date.toLocaleString('ru-RU', {
                            year: 'numeric',
                            month: '2-digit',
                            day: '2-digit',
                            hour: '2-digit',
                            minute: '2-digit',
                            second: '2-digit'
                        }).replace(',', '');
                    }
                }
            }
        },
        scales: {
            x: {
                type: 'time',
                time: {unit: 'minute', stepSize: 10, displayFormats: {minute: 'HH:mm'}},
                min: new Date(Date.now() - 60 * 60 * 1000),
                max: new Date(),
                title: {display: true, text: 'Time', color: '#ececf1'},
                ticks: {color: '#ececf1', stepSize: 10, autoSkip: true},
                grid: {color: 'rgba(255, 255, 255, 0.1)'}
            },
            y: {
                min: 65, max: 185,
                title: {display: true, text: 'Glucose (mg/dL)', color: '#ececf1'},
                ticks: {color: '#ececf1'},
                grid: {color: 'rgba(255, 255, 255, 0.1)'}
            }
        }
    }
});

function updateStatus(connected) {
    document.getElementById('status').className = connected ? 'connected' : 'disconnected';
}

async function fetchInitialData() {
    const now = new Date();
    const oneHourAgo = new Date(now - 60 * 60 * 1000);
    const query = {
        query: `
            query {
                getDataPoints(userId: "${userId}", from: "${oneHourAgo.toISOString()}", to: "${now.toISOString()}") {
                    timestamp
                    sensorGlucose { mgdl }
                    manualGlucose { mgdl }
                }
            }
        `
    };
    const response = await fetch(graphqlEndpoint, {
        method: 'POST',
        headers: {'Content-Type': 'application/json'},
        body: JSON.stringify(query)
    });
    const result = await response.json();
    result.data.getDataPoints.forEach(point => {
        const mgdl = point.sensorGlucose?.mgdl ?? point.manualGlucose?.mgdl;
        if (mgdl !== undefined) {
            dataPoints.push({
                x: new Date(point.timestamp),
                y: mgdl
            });
        }
    });
    updateChart();
}

function connectWebSocket() {
    const client = graphqlWs.createClient({
        url: wsEndpoint,
        retryAttempts: Infinity,
        retryWait: async () => new Promise(resolve => setTimeout(resolve, 1000))
    });

    client.on('connected', () => {
        console.log('WebSocket connected');
        updateStatus(true);
    });

    client.on('closed', () => {
        console.log('WebSocket disconnected');
        updateStatus(false);
    });

    client.subscribe(
        {
            query: `
                subscription {
                    onDataPointAdded(userId: "${userId}") {
                        id
                        userId
                        timestamp
                        sensorGlucose { mgdl }
                        manualGlucose { mgdl }
                    }
                }
            `
        },
        {
            next: ({data}) => {
                const point = data.onDataPointAdded;
                const mgdl = point.sensorGlucose?.mgdl ?? point.manualGlucose?.mgdl;
                if (mgdl !== undefined) {
                    dataPoints.push({x: new Date(point.timestamp), y: mgdl});
                    updateChart();
                }
            },
            error: (error) => console.error('Subscription error:', error),
            complete: () => console.log('Subscription completed')
        }
    );
}

function updateChart() {
    const now = Date.now();
    const oneHourAgo = now - 60 * 60 * 1000;
    chart.data.datasets[0].data = dataPoints.filter(point => point.x >= oneHourAgo);
    chart.options.scales.x.min = oneHourAgo - 60 * 1000;
    chart.options.scales.x.max = now + 60 * 1000;

    chart.options.scales.y.min = Math.round(Math.min(70, ...dataPoints.map(point => point.y))) - 5;
    chart.options.scales.y.max = Math.round(Math.max(180, ...dataPoints.map(point => point.y))) + 5;

    chart.update();
}

fetchInitialData().then(() => connectWebSocket());
