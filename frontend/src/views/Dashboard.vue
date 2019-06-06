<template>
  <div id="pageDashboard">
    <v-container grid-list-xl fluid>
      <!-- Mensagem vazio -->
      <v-layout row wrap v-if="!project.cloned" class="mensagem" transition="slide-x-transition">
        <v-flex xs8 offset-xs2>
          <v-card class="blue-grey--text">
            <v-card-title primary-title class="center">
              <div class="text-md-center center">
                <div class="headline">
                  Welcome to the
                  <strong>Team Tracker</strong>,
                </div>
                <span>Report a project to start tracking!</span>
              </div>
            </v-card-title>
          </v-card>
        </v-flex>
      </v-layout>

      <v-layout row wrap v-if="project.cloned">
        <!-- perspectiva -->
        <v-flex sm12>
          <v-card>
            <v-tabs v-model="active" grow slider-color="primary">
              <v-tab ripple @click="perspective = 'Overview'">Overview</v-tab>
              <v-tab ripple @click="perspective = 'Explore'">Explore</v-tab>
            </v-tabs>
          </v-card>
        </v-flex>

        <!-- Overview -->
        <template v-if="perspective === 'Overview'">
          <!-- cartões -->
          <v-flex lg3 sm6 xs12>
            <mini-statistic icon="check" :title="commits" sub-title="Commits" color="green"></mini-statistic>
          </v-flex>
          <v-flex lg3 sm6 xs12>
            <mini-statistic icon="code" :title="loc" sub-title="Lines of code" color="red"></mini-statistic>
          </v-flex>
          <v-flex lg3 sm6 xs12>
            <mini-statistic
              icon="date_range"
              sub-title="10/10/2017 to 10/10/2018"
              color="light-blue"
            ></mini-statistic>
          </v-flex>
          <v-flex lg3 sm6 xs12>
            <mini-statistic icon="today" :title="activeDays" sub-title="Active days" color="amber"></mini-statistic>
          </v-flex>

          <!-- Grafico -->
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

          <!-- desenvolvedores -->
          <v-flex lg7 sm12 xs12>
            <plain-table></plain-table>
          </v-flex>
        </template>

        <!-- Explore -->
        <template v-if="perspective === 'Explore'">
          <v-flex xs8 offset-xs2>
            <v-card class="blue-grey--text">
              <v-card-title primary-title class="center">
                <div class="text-md-center center">
                  <div class="headline">Explore</div>
                </div>
              </v-card-title>
            </v-card>
          </v-flex>
        </template>
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
    perspective: "Overview",
    commits: 352,
    loc: 5000,
    util: Util,
    activeDays: 47,
    project: { name: "" },
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
  created() {
    window.getApp.$on("UPDATE_PROJECT", project => {
      this.project = project;
      this.tween("commits");
      this.tween("loc");
      this.tween("activeDays");
    });
  },
  mounted() {
    this.colors = this.util
      .getColors()
      .slice(0, this.programmingLanguages.length);
  }
};
</script>
<style lang="stylus">
.center {
  width: 100%;
}

.mensagem {
  padding-top: 40px;
}

.sub-header {
  color: hsla(0, 0%, 100%, 0.7);
}
</style>