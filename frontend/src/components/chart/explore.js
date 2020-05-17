
export default function getExplore(nodeList, linkList) {
  return {
    backgroundColor: '#fff',
    tooltip: {
      formatter: function (x) {
        return x.data.descrition;
      }
    },
    series: [{
      type: 'graph',
      layout: 'force',
      symbolSize: 50,
      edgeSymbol: ['circle', 'arrow'],
      roam: true, // zoom mouse e movimentar grafo 
      focusNodeAdjacency: true, // filtro -> foca em uma conexão e seus nós ou em um nó e suas conexões
      nodeScaleRatio: 0, // o zoom do filtro de foco não impacta os nós
      force: {
        repulsion: 1000,
        edgeLength: [400, 700, 1000],
        layoutAnimation: false
      },
      draggable: true,
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
          fontWeight: 'bold',
          show: true,
          position: 'bottom',
          distance: 0,
          color: '#777',
          backgroundColor: '#ffffff'
        }
      },
      lineStyle: {
        curveness: 0.5,
        normal: {
          width: 2,
          shadowColor: 'none'
        }
      },
      data: nodeList,
      links: linkList,
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