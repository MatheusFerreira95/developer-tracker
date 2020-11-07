export default {
  tooltip: {
    show: true,
    formatter: '{b0}: {d}% (LOC)'
  },
  legend: {
    top: 0,
    textStyle: {
      color: "#3f51b5"
    }
  },
  title: {
    show: true,
    textStyle: {
      color: 'rgba(0, 0, 0 , .87)',
      fontFamily: 'sans-serif'
    }
  },
  grid: {
    containLabel: true,
  },
  xAxis: {
    show: false
  },
  yAxis: {
    show: false
  },
  series: [{
    type: 'pie',
    avoidLabelOverlap: true,
    radius: ['50%', '70%']
  }]
}