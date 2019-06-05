<template>
  <div id="pageDashboard">
    <v-container grid-list-xl fluid>
      <v-layout row wrap>
        <v-flex sm12>
          <v-card>
            <v-toolbar class="elevation-0" color="white">
              <v-toolbar-title class="primary--text">Project Name</v-toolbar-title>
              <v-spacer></v-spacer>
              <v-btn color="primary" dark>
                Explore
                <v-icon dark right>keyboard_arrow_right</v-icon>
              </v-btn>
            </v-toolbar>
          </v-card>
        </v-flex>
        <v-flex lg3 sm6 xs12>
          <mini-statistic icon="check" :title="commits" sub-title="Commits" color="green"></mini-statistic>
        </v-flex>
        <v-flex lg3 sm6 xs12>
          <mini-statistic icon="code" :title="loc" sub-title="Lines of code" color="red"></mini-statistic>
        </v-flex>
        <v-flex lg3 sm6 xs12>
          <mini-statistic icon="date_range" sub-title="10/10/2017 to 10/10/2018" color="light-blue"></mini-statistic>
        </v-flex>
        <v-flex lg3 sm6 xs12>
          <mini-statistic icon="today" :title="activeDays" sub-title="Active days" color="amber"></mini-statistic>
        </v-flex>

        <v-flex lg5 sm12 xs12 v-if="colors.length > 0">
          <v-widget title="Programming Languages" content-bg="white">
            <div slot="widget-content">
              <e-chart
                :path-option="[
                  ['dataset.source', programmingLanguages],
                  ['legend.bottom', '0'],
                  ['color', colors],
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
import EChart from "@/components/chart/echart";
import PlainTable from "@/components/widgets/list/PlainTable";
import MiniStatistic from "@/components/widgets/statistic/MiniStatistic";
import VWidget from "@/components/VWidget";
import Util from "@/util";

export default {
  components: {
    EChart,
    PlainTable,
    VWidget,
    MiniStatistic
  },
  data: () => ({
    title: "oi",
    commits: 352,
    loc: 5000,
    util: Util,
    activeDays: 47,
    programmingLanguages: [
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
      }
    ],
    colors: []
  }),
  methods: {
    tween(propName) {
      var vm = this;

      function animate() {
        if (TWEEN.update()) {
          requestAnimationFrame(animate);
        }
      }
      new TWEEN.Tween({
        tweeningValue: 0
      })
        .to(
          {
            tweeningValue: vm[propName]
          },
          1000
        )
        .onUpdate(function() {
          vm[propName] = this.tweeningValue.toFixed(0);
        })
        .start();
      animate();
    }
  },
  mounted() {
    this.tween("commits");
    this.tween("loc");
    this.tween("activeDays");
    this.colors = this.util
      .getColors()
      .slice(0, this.programmingLanguages.length);
  }
};
</script>
