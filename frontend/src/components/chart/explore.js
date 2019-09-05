/* eslint-disable */

let d = '../../../public/static/dev.png'
/* eslint-enable */
export default function getExplore() {
  return {
    backgroundColor: '#fff',
    title: {
      text: 'titulo do gráfico',
      textStyle: {
        fontWeight: 'normal',
        color: "red",
      }
    },
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
          distance: 30,
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

function getDataChart() {
  let nodes = [{
    name: 'Project',
    img: d
  },
  {
    name: 'Matheus Ferreira',
    img: d
  },
  {
    name: 'Heitor Costa',
    img: d
  },
  {
    name: 'Luana Martins',
    img: d
  },
  {
    name: 'index.js',
    img: d
  },
  {
    name: 'style.css',
    img: d
  },
  {
    name: 'pages',
    img: d
  }
  ]

  let data = [];

  for (let j = 0; j < nodes.length; j++) {
    let node = {
      name: nodes[j].name,
      alarm: nodes[j].alarm,
      // symbol: 'image://' + nodes[j].img,
      itemStyle: {
        normal: {
          color: '#12b5d0',
        }
      }
    }
    data.push(node)
  }

  return data;
}

function getLinks() {
  let links = [{
    source: 'Luana Martins',
    target: 'pages',
    loc: '10 LOC',
    commits: '1 commits'
  }, {
    source: 'Heitor Costa',
    target: 'pages',
    loc: '20 LOC',
    commits: '2 commits'
  },
  {
    source: 'Matheus Ferreira',
    target: 'index.js',
    loc: '30 LOC',
    commits: '3 commits'
  },
  {
    source: 'Heitor Costa',
    target: 'style.css',
    loc: '40 LOC',
    commits: '4 commits'
  },
  {
    source: 'Luana Martins',
    target: 'pages',
    loc: '10 LOC',
    commits: '2 commits'
  }
  ]

  let returnedLinks = [];

  for (let i = 0; i < links.length; i++) {
    let link = {
      source: links[i].source,
      target: links[i].target,
      label: {
        normal: {
          show: true,
          formatter: links[i].loc + "\n" + links[i].commits
        }
      },
      lineStyle: {
        normal: {
          color: 'blue'
        }
      }
    }
    returnedLinks.push(link)
  }

  return returnedLinks;
}
