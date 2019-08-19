var nodes = [{
  name: '服务器',
  img: 'data-1547632686885-o5Rfi3tyk.png',
  alarm: '产生告警了哦'
},
{
  name: '存储设备1',
  img: 'data-1547633192258-2EYYUDdcd.png'
},
{
  name: '存储设备2',
  img: 'data-1547633192258-2EYYUDdcd.png'
},
{
  name: '防火墙',
  img: 'data-1547633094304-08Q_2YEvH.png'
},
{
  name: '网络设备1',
  img: 'data-1547632915872-WZMUphq72.png',
  alarm: '产生告警了哦'
},
{
  name: '网络设备2',
  img: 'data-1547632915872-WZMUphq72.png'
}
]
var links = [{
  source: '存储设备2',
  target: '服务器',
  name: '数据传输'
}, {
  source: '存储设备1',
  target: '服务器',
  name: '数据传输'
},
{
  source: '服务器',
  target: '防火墙',
  name: '访问'
},
{
  source: '防火墙',
  target: '网络设备1',
  name: '访问'
},
{
  source: '防火墙',
  target: '网络设备2',
  name: '访问'
}
]
var charts = {
  nodes: [],
  links: []
}
for (var j = 0; j < nodes.length; j++) {
  var node = {
    name: nodes[j].name,
    alarm: nodes[j].alarm,
    symbol: 'image:///asset/get/s/' + nodes[j].img,
    itemStyle: {
      normal: {
        color: '#12b5d0',
      }
    }
  }
  charts.nodes.push(node)
}
for (var i = 0; i < links.length; i++) {
  var link = {
    source: links[i].source,
    target: links[i].target,
    label: {
      normal: {
        show: true,
        formatter: links[i].name
      }
    },
    lineStyle: {
      normal: {
        color: '#17FFF3'
      }
    }
  }
  charts.links.push(link)
}
option = {
  backgroundColor: '#000033',
  title: {
    text: '力导向拓扑图',
    textStyle: {
      fontWeight: 'normal',
      color: "rgb(255,224,0)",
    }
  },
  tooltip: {
    formatter: function (x) {
      return x.data.des;
    }
  },
  series: [{
    type: 'graph',
    layout: 'force',
    symbolSize: 50,
    edgeSymbol: ['circle', 'arrow'],
    force: {
      repulsion: 1000,
      edgeLength: [150, 200],
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
        show: true,
        position: 'bottom',
        color: '#17FFF3'
      }
    },
    lineStyle: {
      normal: {
        width: 2,
        shadowColor: 'none'
      }
    },
    data: charts.nodes,
    links: charts.links,
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
};
// 用于告警的动态效果
setInterval(() => {
  var dataI = []
  for (var n = 0; n < nodes.length; n++) {
    var alarm = nodes[n].alarm
    if (alarm !== null && alarm !== '' && alarm !== undefined) {
      option.series[0].data[n].symbolSize = 60
      option.series[0].data[n].label = {
        normal: {
          color: 'rgb(255,224,0)'
        }
      }
      option.series[0].itemStyle.normal.label.formatter = function (item) {
        if (item.data.alarm !== undefined) {
          return item.data.name + '\n[ ' + item.data.alarm + ' ]'
        } else {
          return item.data.name
        }
      }
      dataI.push(n)
    }
  }
  myChart.setOption(option)
  setTimeout(() => {
    for (var m = 0; m < dataI.length; m++) {
      option.series[0].data[dataI[m]].symbolSize = 50
      option.series[0].data[dataI[m]].label = {
        normal: {
          color: '#17FFF3'
        }
      }
      option.series[0].itemStyle.normal.label.formatter = function (item) {
        return item.data.name
      }
    }
    myChart.setOption(option)
  }, 500)
}, 1000)