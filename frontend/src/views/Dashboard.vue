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
              <v-tab ripple @click="perspective = 'Overview'">Project</v-tab>
              <v-tab ripple @click="perspective = 'Explore'">Developer</v-tab>
            </v-tabs>
          </v-card>
        </v-flex>

        <!-- Overview -->
        <template v-if="perspective === 'Overview'">
          <!-- cartões -->
          <v-flex lg6 sm6 xs12>
            <mini-statistic
              icon="check"
              :title="project.numCommits"
              sub-title="Commits"
              color="green"
            ></mini-statistic>
          </v-flex>
          <v-flex lg6 sm6 xs12>
            <mini-statistic
              icon="code"
              :title="project.numLoc"
              sub-title="Lines of code"
              color="red"
            ></mini-statistic>
          </v-flex>
          <!-- <v-flex lg3 sm6 xs12>
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
          </v-flex>-->

          <!-- Grafico Linguagem de programacao-->
          <v-flex lg6 sm12 xs12>
            <v-widget title="Programming Languages" content-bg="white">
              <div slot="widget-content">
                <chart
                  v-if="project.numLocProgrammingLanguageList.length > 0"
                  :options="optionsChartProgrammingLanguage"
                  :init-options="initOptions"
                  ref="pie"
                  autoresize
                />
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
                          <!-- <v-icon dark medium :color="util.getColors()[props.item.avatar]">person</v-icon> -->
                          <v-icon v-if="props.item.truckFactor" right color="amber">start</v-icon>
                        </td>
                        <td
                          class="text-xs-left"
                        >{{ props.item.name + " (" + props.item.email + ")"}}</td>
                        <!-- <td class="text-xs-left">{{ props.item.numLoc + ""}}</td> -->
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
          <v-flex lg12 sm12 xs12>
            <v-widget title content-bg="white" :title2="history">
              <div slot="widget-content">
                <chart
                  v-if="explore !== null"
                  :options="explore"
                  :init-options="initOptions"
                  ref="explore"
                  autoresize
                />
                <div v-else>Does not apply to this project</div>
              </div>
            </v-widget>
          </v-flex>
        </template>
      </v-layout>
    </v-container>
  </div>
</template>

<script>
import pie from "@/components/chart/pie";
import getExplore from "@/components/chart/explore";
import MiniStatistic from "@/components/widgets/statistic/MiniStatistic";
import VWidget from "@/components/VWidget";
import Util from "@/util";
import ECharts from "@/components/chart/ECharts.vue";
import "echarts";
import { getExploreProject } from "@/api/project";
/* icones svg encontrados em http://svgicons.sparkk.fr/ */
let projectIcon =
  "M17.283,5.549h-5.26V4.335c0-0.222-0.183-0.404-0.404-0.404H8.381c-0.222,0-0.404,0.182-0.404,0.404v1.214h-5.26c-0.223,0-0.405,0.182-0.405,0.405v9.71c0,0.223,0.182,0.405,0.405,0.405h14.566c0.223,0,0.404-0.183,0.404-0.405v-9.71C17.688,5.731,17.506,5.549,17.283,5.549 M8.786,4.74h2.428v0.809H8.786V4.74z M16.879,15.26H3.122v-4.046h5.665v1.201c0,0.223,0.182,0.404,0.405,0.404h1.618c0.222,0,0.405-0.182,0.405-0.404v-1.201h5.665V15.26z M9.595,9.583h0.81v2.428h-0.81V9.583zM16.879,10.405h-5.665V9.19c0-0.222-0.183-0.405-0.405-0.405H9.191c-0.223,0-0.405,0.183-0.405,0.405v1.215H3.122V6.358h13.757V10.405z";
let directoryIcon =
  "M17.927,5.828h-4.41l-1.929-1.961c-0.078-0.079-0.186-0.125-0.297-0.125H4.159c-0.229,0-0.417,0.188-0.417,0.417v1.669H2.073c-0.229,0-0.417,0.188-0.417,0.417v9.596c0,0.229,0.188,0.417,0.417,0.417h15.854c0.229,0,0.417-0.188,0.417-0.417V6.245C18.344,6.016,18.156,5.828,17.927,5.828 M4.577,4.577h6.539l1.231,1.251h-7.77V4.577z M17.51,15.424H2.491V6.663H17.51V15.424z";
let developerIcon =
  "M12.075,10.812c1.358-0.853,2.242-2.507,2.242-4.037c0-2.181-1.795-4.618-4.198-4.618S5.921,4.594,5.921,6.775c0,1.53,0.884,3.185,2.242,4.037c-3.222,0.865-5.6,3.807-5.6,7.298c0,0.23,0.189,0.42,0.42,0.42h14.273c0.23,0,0.42-0.189,0.42-0.42C17.676,14.619,15.297,11.677,12.075,10.812 M6.761,6.775c0-2.162,1.773-3.778,3.358-3.778s3.359,1.616,3.359,3.778c0,2.162-1.774,3.778-3.359,3.778S6.761,8.937,6.761,6.775 M3.415,17.69c0.218-3.51,3.142-6.297,6.704-6.297c3.562,0,6.486,2.787,6.705,6.297H3.415z";
let fileIcon =
  "M15.475,6.692l-4.084-4.083C11.32,2.538,11.223,2.5,11.125,2.5h-6c-0.413,0-0.75,0.337-0.75,0.75v13.5c0,0.412,0.337,0.75,0.75,0.75h9.75c0.412,0,0.75-0.338,0.75-0.75V6.94C15.609,6.839,15.554,6.771,15.475,6.692 M11.5,3.779l2.843,2.846H11.5V3.779z M14.875,16.75h-9.75V3.25h5.625V7c0,0.206,0.168,0.375,0.375,0.375h3.75V16.75z";

export default {
  components: {
    VWidget,
    MiniStatistic,
    chart: ECharts
  },
  data: () => ({
    perspective: "Overview",
    util: Util,
    history: {
      paths: []
    },
    pie,
    explore: null,
    optionsChartProgrammingLanguage: null,
    initOptions: {
      renderer: "canvas"
    },
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
        text: "Truck Factor", // avatar
        value: "truckFactor"
      },
      { text: "Name (email)", value: "name" }
      // { text: "NLOC", value: "numLoc" }
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
      this.pie.dataset = {};
      this.pie.dataset.source = this.project.numLocProgrammingLanguageList;
      this.optionsChartProgrammingLanguage = { ...pie };

      this.tween("numCommits");
      this.tween("numLoc");
    },

    buildExplore(nodeData) {
      window.getApp.$emit("START_LOADING");
      let filter = {
        directory: "",
        localRepository: this.project.localRepository,
        remoteRepository: this.project.remoteRepository,
        zoomPath: nodeData === null ? "Project/" : nodeData.descrition + "/"
      };

      if (nodeData !== null) this.updateHistory(nodeData);

      getExploreProject(filter)
        .then(
          response => {
            this.setExplore(response.data);
            window.getApp.$emit("STOP_LOADING");
          },
          error => {
            alert("Erro: " + error);
            window.getApp.$emit("STOP_LOADING");
          }
        )
        .catch(function(error) {
          window.getApp.$emit("STOP_LOADING");
        });
    },

    updateHistory(nodeData) {
      let newHistory = [];
      let exist = false;

      this.history.paths.forEach(item => {
        if (item.descrition === nodeData.descrition) {
          exist = true;
        }
      });

      if (exist) {
        let i = 0;
        while (i <= this.history.paths.indexOf(nodeData)) {
          newHistory.push(this.history.paths[i]);
          i++;
        }
        this.history.paths = newHistory;
      } else {
        this.history.paths.push(nodeData);
      }
    },

    setExplore(explore) {
      for (let i = 0; i < explore.linkList.length; i++) {
        explore.linkList[i].value = explore.linkList[i].numLoc;
        explore.linkList[i].label = {
          normal: {
            show: true,
            fontWeight: "bold",
            backgroundColor: "#ffffff",
            formatter:
              explore.linkList[i].loc + " (" + explore.linkList[i].commits + ")"
          }
        };
        explore.linkList[i].lineStyle = {
          normal: {
            color: explore.linkList[i].color
          }
        };
      }

      for (let j = 0; j < explore.nodeList.length; j++) {
        explore.nodeList[j].value = explore.nodeList[j].numLoc;
        explore.nodeList[j].itemStyle = {
          normal: {
            color: explore.nodeList[j].color
          }
        };
      }

      this.explore = getExplore(explore.nodeList, explore.linkList);
    }
  },
  created() {
    let that = this;
    window.getApp.$on("UPDATE_PROJECT", updated => {
      if (updated.componentSubType && updated.componentSubType === "graph") {
        if (
          updated.data.nodeType === "Folder" ||
          updated.data.nodeType === "Project"
        ) {
          that.buildExplore(updated.data);
        }
      } else {
        that.setProject(updated);
      }
    });
    window.getApp.$on("APP_LEVEL_CHANGE", index => {
      if (index + 1 === this.history.paths.length) return;
      if (index === "init") {
        that.buildExplore(null);
        return;
      }

      let nodeData = this.history.paths[index];
      that.buildExplore(nodeData);
    });
  },
  watch: {
    perspective: function(val) {
      if (val === "Explore") {
        this.history.paths = [];
        this.buildExplore(null);
      }
    }
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
