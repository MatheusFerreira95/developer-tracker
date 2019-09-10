
export default function getExplore(getDataChart, getLinks) {
  return {
    backgroundColor: '#fff',
    tooltip: {
      formatter: function (x) {
        return x.data.name;
      }
    },
    series: [{
      type: 'graph',
      layout: 'force',
      symbolSize: 50,
      edgeSymbol: ['circle', 'arrow'],
      force: {
        repulsion: 500,
        edgeLength: [150, 200],
        layoutAnimation: false
      },
      draggable: false,
      edgeLabel: {
        normal: {
          show: true,
          formatter: function (x) {
            return x.data.name;
          }
        }
      },
      label: {
        normal: {
          show: true,
          position: 'bottom',
          color: '#777'
        }
      },
      lineStyle: {
        normal: {
          width: 2,
          shadowColor: 'none'
        }
      },
      data: getDataChart(),
      links: getLinks(),
      itemStyle: {
        normal: {
          label: {
            show: true,
            formatter: function (item) {
              return item.data.name
            }
          }
        }
      }
    }]
  }
}