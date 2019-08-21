<template>
  <div id="pageDashboard">
    <v-container grid-list-xl fluid>
      <!-- Mensagem vazio -->
      <v-layout
        row
        wrap
        v-if="!project.localRepository"
        class="mensagem"
        transition="slide-x-transition"
      >
        <v-flex xs8 offset-xs2>
          <v-card class="blue-grey--text">
            <v-card-title primary-title class="center">
              <div class="text-md-center center">
                <div class="headline">
                  Welcome to the
                  <strong>Developer Tracker</strong>,
                </div>
                <span>Report a project to start tracking!</span>
              </div>
            </v-card-title>
          </v-card>
        </v-flex>
      </v-layout>

      <v-layout row wrap v-if="project.localRepository">
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
            <mini-statistic
              icon="check"
              :title="project.numCommits"
              sub-title="Commits"
              color="green"
            ></mini-statistic>
          </v-flex>
          <v-flex lg3 sm6 xs12>
            <mini-statistic
              icon="code"
              :title="project.numLoc"
              sub-title="Lines of code"
              color="red"
            ></mini-statistic>
          </v-flex>
          <v-flex lg3 sm6 xs12>
            <mini-statistic
              icon="date_range"
              title=" "
              :sub-title="'First commit: ' + project.firstCommit"
              color="light-blue"
            ></mini-statistic>
          </v-flex>
          <v-flex lg3 sm6 xs12>
            <mini-statistic
              icon="today"
              title=" "
              :sub-title="'Last commit: ' + project.lastCommit"
              color="amber"
            ></mini-statistic>
          </v-flex>

          <!-- Grafico Linguagem de programacao-->
          <v-flex lg6 sm12 xs12>
            <v-widget title="Programming Languages" content-bg="white">
              <div slot="widget-content">
                <e-chart
                  :path-option="[
                  ['dataset.source', project.numLocProgrammingLanguageList],
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
                  v-if="project.numLocProgrammingLanguageList.length > 0"
                ></e-chart>
                <div v-else>Does not apply to this project</div>
              </div>
            </v-widget>
          </v-flex>

          <!-- desenvolvedores -->
          <v-flex lg6 sm12 xs12>
            <v-card>
              <v-toolbar card dense color="transparent">
                <v-toolbar-title>
                  <h4>Developers</h4>
                </v-toolbar-title>
                <v-divider class="white"></v-divider>
                <v-chip color="indigo" text-color="white">
                  <v-icon color="amber">start</v-icon>
                  Truck Factor: {{project.truckFactor}}
                </v-chip>
              </v-toolbar>
              <v-divider></v-divider>
              <v-card-text class="pa-0">
                <template>
                  <v-data-table
                    :headers="headers"
                    :items="project.developerList"
                    class="elevation-0"
                    hide-actions
                    item-key="name"
                    disable-initial-sort
                  >
                    <template slot="items" slot-scope="props">
                      <tr>
                        <td class="avatar-developer">
                          <v-icon dark medium :color="util.getColors()[props.item.avatar]">person</v-icon>
                          <v-icon v-if="props.item.truckFactor" right color="amber">start</v-icon>
                        </td>
                        <td
                          class="text-xs-left"
                        >{{ props.item.name + " (" + props.item.email + ")"}}</td>
                        <td class="text-xs-left">{{ props.item.percentLoc + ""}}</td>
                      </tr>
                    </template>
                  </v-data-table>
                </template>
                <v-divider></v-divider>
              </v-card-text>
            </v-card>
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
import MiniStatistic from "@/components/widgets/statistic/MiniStatistic";
import VWidget from "@/components/VWidget";
import Util from "@/util";

export default {
  components: {
    EChart,
    VWidget,
    MiniStatistic
  },
  data: () => ({
    perspective: "Overview",
    util: Util,
    project: {
      numLoc: 0,
      numCommits: 0,
      firstCommit: "",
      LastCommit: "",
      numLocProgrammingLanguageList: [],
      developerList: [],
      localRepository: ""
    },
    colors: [],
    headers: [
      {
        text: "",
        align: "center",
        value: "truckFactor"
      },
      { text: "Name (email)", value: "name" },
      { text: "LOC (%)", value: "numLoc" }
    ]
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
            tweeningValue: vm.project[propName]
          },
          1000
        )
        .onUpdate(function() {
          vm.project[propName] = this.tweeningValue.toFixed(0);
        })
        .start();
      animate();
    },
    setProject(newProject) {
      this.project = newProject;

      this.colors = this.util
        .getColors()
        .slice(0, this.project.numLocProgrammingLanguageList.length);

      window.getApp.$emit("UPDATE_CHART");

      this.tween("numCommits");
      this.tween("numLoc");
    }
  },
  created() {
    window.getApp.$on("UPDATE_PROJECT", project => {
      this.setProject(project);
    });
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

.click-pointer {
  cursor: pointer;
}

.avatar-developer {
  display: flex;
}
</style>
