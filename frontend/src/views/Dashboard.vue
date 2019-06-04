<template>
  <div id="pageDashboard">
    <v-container grid-list-xl fluid>
      <v-layout row wrap>
        <v-flex lg3 sm6 xs12>
          <mini-statistic icon="check" title="100+" sub-title="Commits" color="green"></mini-statistic>
        </v-flex>
        <v-flex lg3 sm6 xs12>
          <mini-statistic icon="code" title="150+" sub-title="Lines of code" color="red"></mini-statistic>
        </v-flex>
        <v-flex lg3 sm6 xs12>
          <mini-statistic icon="date_range" sub-title="10/10/2017 to 10/10/2018" color="light-blue"></mini-statistic>
        </v-flex>
        <v-flex lg3 sm6 xs12>
          <mini-statistic icon="today" title="50+" sub-title="Active days" color="purple"></mini-statistic>
        </v-flex>

        <v-flex lg5 sm12 xs12>
          <v-widget title="Programming Languages" content-bg="white">
            <div slot="widget-content">
              <e-chart
                :path-option="[
                  ['dataset.source', locationData],
                  ['legend.bottom', '0'],
                  ['color', [color.lightBlue.base, color.indigo.base, color.pink.base, color.green.base, color.cyan.base, color.teal.base]],
                  ['xAxis.show', false],
                  ['yAxis.show', false],
                  ['series[0].type', 'pie'],
                  ['series[0].avoidLabelOverlap', true],         
                  ['series[0].radius', ['50%', '70%']],                      
                ]"
                height="400px"
                width="100%"
              ></e-chart>
            </div>
          </v-widget>
        </v-flex>

        <v-flex lg7 sm12 xs12>
          <plain-table></plain-table>
        </v-flex>
      </v-layout>
    </v-container>
  </div>
</template>

<script>
import API from "@/api";
import EChart from "@/components/chart/echart";
import PlainTable from "@/components/widgets/list/PlainTable";
import Material from "vuetify/es5/util/colors";
import MiniStatistic from "@/components/widgets/statistic/MiniStatistic";
import VWidget from "@/components/VWidget";

export default {
  components: {
    EChart,
    PlainTable,
    Material,
    VWidget,
    MiniStatistic
  },
  data: () => ({
    color: Material,
    locationData: [
        {
          value: 50,
          name: "Java"
        },
        {
          value: 35,
          name: "JavaScript"
        },
        {
          value: 25,
          name: "HTML"
        },
        {
          value: 10,
          name: "CSS"
        },
        {
          value: 10,
          name: "Shell"
        }
      ]
  })
};
</script>
