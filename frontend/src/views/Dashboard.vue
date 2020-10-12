<template>
  <div id="pageDashboard">
    <v-container grid-list-xl fluid>
      <!-- second loading -->
      <v-dialog
          v-model="secondLoading"
          persistent
          fullscreen
          content-class="loading-dialog"
        >
          <v-container fill-height>
            <v-layout row justify-center align-center>
              <v-progress-circular
                indeterminate
                :size="90"
                :width="3"
                color="primary"
              ></v-progress-circular>
            </v-layout>
          </v-container>
        </v-dialog>
      <!-- Mensagem vazio -->
      <v-layout
        row
        wrap
        v-if="!projectVersions.projectVersion1.localRepository"
        class="mensagem"
        transition="slide-x-transition"
      >
        <v-flex xs8 offset-xs2>
          <v-card class="blue-grey--text">
            <v-card-title primary-title class="center">
              <div class="text-md-center center">
                <div class="headline">
                  Welcome to the
                  <strong
                    >Developer Tracker App
                    {{
                      projectVersions.projectVersion1.localRepository
                    }}</strong
                  >,
                </div>
                <span>Report a project to start tracking!</span>
              </div>
            </v-card-title>
          </v-card>
        </v-flex>
      </v-layout>

      <!-- tabs e labels de versao -->
      <v-layout
        row
        wrap
        v-if="projectVersions.projectVersion1.localRepository"
      >
        <v-flex sm12>
          <v-card>
            <v-tabs v-model="active" grow slider-color="primary">
              <v-tab ripple @click="perspective = 'Overview'"
                >Project
              </v-tab>
              <v-tab ripple @click="perspective = 'Explore'"
                >Developer</v-tab
              >
            </v-tabs>
          </v-card>
        </v-flex>

        <v-flex v-if="!projectVersions.projectVersion2.currentVersion" xs12 style="text-align: center; color: gray">
          {{ projectVersions.projectVersion1.currentVersion }}
        </v-flex>
        </v-flex>
        <v-flex v-if="projectVersions.projectVersion2.currentVersion" lg5 sm5 xs12 style="text-align: center; color: gray">
          {{ projectVersions.projectVersion1.currentVersion }}
        </v-flex>
        <v-flex v-if="projectVersions.projectVersion2.currentVersion" lg2 sm2 xs12 style="text-align: center; color: gray">
          x
        </v-flex>
        <v-flex v-if="projectVersions.projectVersion2.currentVersion" lg5 sm5 xs12 style="text-align: center; color: gray">
          {{ projectVersions.projectVersion2.currentVersion }}
        </v-flex>
      </v-layout>

      <!-- uma versao -->
      <v-layout
        row
        wrap
        v-show="
          projectVersions.projectVersion1.localRepository &&
          !projectVersions.projectVersion2.localRepository
        "
      >
        <!-- Overview -->
        <template v-if="perspective === 'Overview'">
          <!-- cartões -->
          <v-flex lg6 sm6 xs12>
            <mini-statistic
              icon="check"
              :title="projectVersions.projectVersion1.numCommits"
              sub-title="Commits"
              color="green"
            ></mini-statistic>
          </v-flex>
          <v-flex lg6 sm6 xs12>
            <mini-statistic
              icon="code"
              :title="projectVersions.projectVersion1.numLoc"
              sub-title="Lines of code"
              color="red"
            ></mini-statistic>
          </v-flex>
          <!-- <v-flex lg3 sm6 xs12>
            <mini-statistic
              icon="date_range"
              title=" "
              :sub-title="'First commit: ' + projectVersions.projectVersion1.firstCommit"
              color="light-blue"
            ></mini-statistic>
          </v-flex>
          <v-flex lg3 sm6 xs12>
            <mini-statistic
              icon="today"
              title=" "
              :sub-title="'Last commit: ' + projectVersions.projectVersion1.lastCommit"
              color="amber"
            ></mini-statistic>
          </v-flex>-->

          <!-- Grafico Linguagem de programacao-->
          <v-flex lg6 sm12 xs12>
            <v-widget title="Programming Languages" content-bg="white">
              <div slot="widget-content">
                <chart
                  v-if="
                    projectVersions.projectVersion1
                      .numLocProgrammingLanguageList.length > 0
                  "
                  :options="optionsChartProgrammingLanguage1"
                  :init-options="initOptions"
                  ref="pie1"
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
                <v-chip color="lightblue" text-color="gray">
                  <v-icon color="gray">local_shipping</v-icon>
                  &nbsp;&nbsp;&nbsp;Truck Factor:
                  {{ projectVersions.projectVersion1.truckFactor }}
                </v-chip>
              </v-toolbar>
              <v-divider></v-divider>
              <v-card-text class="pa-0">
                <template>
                  <v-data-table
                    :headers="headers"
                    :items="projectVersions.projectVersion1.developerList"
                    class="elevation-0"
                    hide-actions
                    item-key="name"
                    disable-initial-sort
                  >
                    <template slot="items" slot-scope="props">
                      <tr>
                        <td class="avatar-developer">
                          <!-- <v-icon dark medium :color="util.getColors()[props.item.avatar]">person</v-icon> -->
                          <v-icon
                            v-if="props.item.truckFactor"
                            right
                            color="gray"
                            >local_shipping</v-icon
                          >
                        </td>
                        <td class="text-xs-left">
                          {{ props.item.name + " (" + props.item.email + ")" }}
                        </td>
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
                  v-if="explore1 !== null"
                  :options="explore1"
                  :init-options="initOptions"
                  ref="explore1"
                  autoresize
                />
                <div v-else>Does not apply to this project</div>
              </div>
            </v-widget>
          </v-flex>
        </template>
      </v-layout>

      <!-- duas versoes: comparativo -->
      <v-layout
        row
        wrap
        v-show="
          projectVersions.projectVersion1.localRepository &&
          projectVersions.projectVersion2.localRepository
        "
      >
        <!-- Overview -->
        <template v-if="perspective === 'Overview'">
          <!-- cartões -->
          <v-flex lg6 sm6 xs12>
            <mini-statistic
              icon="check"
              :title="projectVersions.projectVersion1.numCommits"
              sub-title="Commits"
              color="green"
            ></mini-statistic>
          </v-flex>
          <v-flex lg6 sm6 xs12>
            <mini-statistic
              icon="check"
              :title="projectVersions.projectVersion2.numCommits"
              sub-title="Commits"
              color="green"
            ></mini-statistic>
          </v-flex>
          <v-flex lg6 sm6 xs12>
            <mini-statistic
              icon="code"
              :title="projectVersions.projectVersion1.numLoc"
              sub-title="Lines of code"
              color="red"
            ></mini-statistic>
          </v-flex>
          <v-flex lg6 sm6 xs12>
            <mini-statistic
              icon="code"
              :title="projectVersions.projectVersion2.numLoc"
              sub-title="Lines of code"
              color="red"
            ></mini-statistic>
          </v-flex>

          <!-- Grafico Linguagem de programacao-->
          <v-flex lg6 sm6 xs12>
            <v-widget title="Programming Languages" content-bg="white">
              <div slot="widget-content">
                <chart
                  v-if="
                    projectVersions.projectVersion1
                      .numLocProgrammingLanguageList.length > 0
                  "
                  :options="optionsChartProgrammingLanguage1"
                  :init-options="initOptions"
                  ref="pie1"
                  autoresize
                />
                <div v-else>Does not apply to this project</div>
              </div>
            </v-widget>
          </v-flex>
          <v-flex lg6 sm6 xs12>
            <v-widget title="Programming Languages" content-bg="white">
              <div slot="widget-content">
                <chart
                  v-if="
                    projectVersions.projectVersion2
                      .numLocProgrammingLanguageList.length > 0
                  "
                  :options="optionsChartProgrammingLanguage2"
                  :init-options="initOptions"
                  ref="pie2"
                  autoresize
                />
                <div v-else>Does not apply to this project</div>
              </div>
            </v-widget>
          </v-flex>

          <!-- desenvolvedores -->
          <v-flex lg6 sm6 xs12>
            <v-card>
              <v-toolbar card dense color="transparent">
                <v-toolbar-title>
                  <h4>Developers</h4>
                </v-toolbar-title>
                <v-divider class="white"></v-divider>
                <v-chip color="lightblue" text-color="gray">
                  <v-icon color="gray">local_shipping</v-icon>
                  &nbsp;&nbsp;&nbsp;Truck Factor:
                  {{ projectVersions.projectVersion1.truckFactor }}
                </v-chip>
              </v-toolbar>
              <v-divider></v-divider>
              <v-card-text class="pa-0">
                <template>
                  <v-data-table
                    :headers="headers"
                    :items="projectVersions.projectVersion1.developerList"
                    class="elevation-0"
                    hide-actions
                    item-key="name"
                    disable-initial-sort
                  >
                    <template slot="items" slot-scope="props">
                      <tr>
                        <td class="avatar-developer">
                          <!-- <v-icon dark medium :color="util.getColors()[props.item.avatar]">person</v-icon> -->
                          <v-icon
                            v-if="props.item.truckFactor"
                            right
                            color="gray"
                            >local_shipping</v-icon
                          >
                        </td>
                        <td class="text-xs-left">
                          {{ props.item.name + " (" + props.item.email + ")" }}
                        </td>
                        <!-- <td class="text-xs-left">{{ props.item.numLoc + ""}}</td> -->
                      </tr>
                    </template>
                  </v-data-table>
                </template>
                <v-divider></v-divider>
              </v-card-text>
            </v-card>
          </v-flex>
          <v-flex lg6 sm6 xs12>
            <v-card>
              <v-toolbar card dense color="transparent">
                <v-toolbar-title>
                  <h4>Developers</h4>
                </v-toolbar-title>
                <v-divider class="white"></v-divider>
                <v-chip color="lightblue" text-color="gray">
                  <v-icon color="gray">local_shipping</v-icon>
                  &nbsp;&nbsp;&nbsp;Truck Factor:
                  {{ projectVersions.projectVersion2.truckFactor }}
                </v-chip>
              </v-toolbar>
              <v-divider></v-divider>
              <v-card-text class="pa-0">
                <template>
                  <v-data-table
                    :headers="headers"
                    :items="projectVersions.projectVersion2.developerList"
                    class="elevation-0"
                    hide-actions
                    item-key="name"
                    disable-initial-sort
                  >
                    <template slot="items" slot-scope="props">
                      <tr>
                        <td class="avatar-developer">
                          <!-- <v-icon dark medium :color="util.getColors()[props.item.avatar]">person</v-icon> -->
                          <v-icon
                            v-if="props.item.truckFactor"
                            right
                            color="gray"
                            >local_shipping</v-icon
                          >
                        </td>
                        <td class="text-xs-left">
                          {{ props.item.name + " (" + props.item.email + ")" }}
                        </td>
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
          <v-flex lg6 sm6 xs12>
            <v-widget title content-bg="white" :title2="history">
              <div slot="widget-content">
                <chart
                  v-if="explore1 !== null"
                  :options="explore1"
                  :init-options="initOptions"
                  ref="explore1"
                  autoresize
                />
                <div v-else>Does not apply to this project</div>
              </div>
            </v-widget>
          </v-flex>
          <v-flex lg6 sm6 xs12>
            <v-widget
              title
              content-bg="white"
              :title2="history"
              :showInteractions="false"
            >
              <div slot="widget-content">
                <chart
                  v-if="explore2 !== null"
                  :options="explore2"
                  :init-options="initOptions"
                  ref="explore2"
                  autoresize
                />
                <div v-else>Does not apply to this project</div>
              </div>
            </v-widget>
          </v-flex>
        </template>
      </v-layout>
    </v-container>

    <!-- 1. Recomendation commit and loc -->
    <div
      class="text-center"
      style="float: right"
      v-if="
        perspective === 'Overview' &&
        projectVersions.projectVersion1.developerList.length > 0
      "
    >
      <v-menu open-on-hover left offset-x>
        <template v-slot:activator="{ on }">
          <v-icon
            right
            v-on="on"
            color="orange"
            class="fa-blink"
            style="position: absolute; top: 150px; font-size: 25px; right: 2px"
            >assistant</v-icon
          >
        </template>

        <v-card>
          <v-toolbar card dense color="transparent">
            <v-toolbar-title>
              <h4>Project Commits and LOC</h4>
            </v-toolbar-title>
          </v-toolbar>
          <v-divider></v-divider>
          <v-card-text class="pa-0">
            <template>
              <span>
                <br />&nbsp;Use to understand the dimension of the project. It
                serves as a parameter to analyze the connections between
                developers and artifacts. &nbsp;
                <br />
              </span>

              <span>
                <br />
                <b>&nbsp; Other notes</b> (This helps to mitigate the risk of
                developers distorting their work to fit the metrics):
                <br />
                <span
                  >&nbsp;- When observing LOC (project and developers), consider
                  that the team must follow the appropriate code standards of
                  the programming language (e.g. placement of "{}"). Also
                  consider defining a code review process, so that other
                  developers can evaluate the solutions implemented by a team
                  member, avoiding inappropriate or excessive LOC
                  solutions.</span
                >
                <br />
                <span
                  >&nbsp;- When observing commmits, consider that the team must
                  follow a commits pattern (e.g. as atomic as possible).</span
                >
                <br />
              </span>
            </template>
          </v-card-text>
        </v-card>
      </v-menu>
    </div>

    <!-- 2. Recomendation programmimg lang -->
    <div
      class="text-center"
      style="float: left"
      v-if="
        perspective === 'Overview' &&
        projectVersions.projectVersion1.developerList.length > 0
      "
    >
      <v-menu open-on-hover left offset-x>
        <template v-slot:activator="{ on }">
          <v-icon
            right
            v-on="on"
            color="orange"
            class="fa-blink"
            style="position: absolute; top: 350px; font-size: 25px; left: -5px"
            >assistant</v-icon
          >
        </template>

        <v-card>
          <v-toolbar card dense color="transparent">
            <v-toolbar-title>
              <h4>Project Programming Languages</h4>
            </v-toolbar-title>
          </v-toolbar>
          <v-divider></v-divider>
          <v-card-text class="pa-0">
            <template>
              <span>
                <br />&nbsp;Use to understand the demand for technology in the
                project. Consider the different characteristics of programming
                languages ​​when analyzing this, some languages ​​may require
                more LOC due to their characteristics. &nbsp;
                <br />
              </span>

              <span>
                <br />
                <b>&nbsp; Other notes</b> (This helps to mitigate the risk of
                developers distorting their work to fit the metrics):
                <br />
                <span
                  >&nbsp;- When observing LOC (project and developers), consider
                  that the team must follow the appropriate code standards of
                  the programming language (e.g. placement of "{}"). Also
                  consider defining a code review process, so that other
                  developers can evaluate the solutions implemented by a team
                  member, avoiding inappropriate or excessive LOC
                  solutions.</span
                >
                <br />
              </span>
            </template>
          </v-card-text>
        </v-card>
      </v-menu>
    </div>

    <!-- 3. Recomendation Project Truck Factor -->
    <div
      class="text-center"
      style="float: right"
      v-if="
        perspective === 'Overview' &&
        projectVersions.projectVersion1.developerList.length > 0
      "
    >
      <v-menu open-on-hover left offset-x>
        <template v-slot:activator="{ on }">
          <v-icon
            right
            v-on="on"
            color="orange"
            class="fa-blink"
            style="position: absolute; top: 350px; font-size: 25px; right: 2px"
            >assistant</v-icon
          >
        </template>

        <v-card>
          <v-toolbar card dense color="transparent">
            <v-toolbar-title>
              <h4>Project Truck Factor</h4>
            </v-toolbar-title>
          </v-toolbar>
          <v-divider></v-divider>
          <v-card-text class="pa-0">
            <template>
              <span>
                <br />&nbsp;The Truck Factor is calculated based on the degree
                of authorship of the developers in the project files. Consider
                that Truck Factor developers can concentrate knowledge on more
                than half of the project's files. The lower the value of the
                Truck Factor, the greater the concentration of knowledge. To
                mitigate the concentration of knowledge, consider including
                practices such as pair programming and running people in the
                source code. &nbsp;
                <br />
              </span>
            </template>
          </v-card-text>
        </v-card>
      </v-menu>
    </div>

    <!-- 4. Recomendations developers -->
    <div
      class="text-center"
      style="float: left"
      v-if="
        perspective === 'Explore' &&
        projectVersions.projectVersion1.developerList.length > 0
      "
    >
      <v-menu open-on-hover right offset-y>
        <template v-slot:activator="{ on }">
          <v-icon
            right
            v-on="on"
            color="orange"
            class="fa-blink"
            style="position: absolute; top: 250px; font-size: 25px; left: -5px"
            >assistant</v-icon
          >
        </template>

        <v-card>
          <v-toolbar card dense color="transparent">
            <v-toolbar-title>
              <h4>Truck Factor developers</h4>
            </v-toolbar-title>
          </v-toolbar>
          <v-divider></v-divider>
          <v-card-text class="pa-0">
            <template>
              <span>
                <br />&nbsp;They are team members who can have a high degree of
                authorship and knowledge about the project version. Note in
                which regions of the source code practices such as practices
                such as pair programming and running people can be applied to
                distribute the concentration of knowledge. &nbsp;
                <br />
              </span>
            </template>
          </v-card-text>
        </v-card>
      </v-menu>
    </div>

    <!-- 5. Recomendations developers -->
    <div
      class="text-center"
      style="float: right"
      v-if="
        perspective === 'Explore' &&
        projectVersions.projectVersion1.developerList.length > 0
      "
    >
      <v-menu open-on-hover right offset-y>
        <template v-slot:activator="{ on }">
          <v-icon
            right
            v-on="on"
            color="orange"
            class="fa-blink"
            style="position: absolute; top: 325px; font-size: 25px; left: -5px"
            >assistant</v-icon
          >
        </template>

        <v-card>
          <v-toolbar card dense color="transparent">
            <v-toolbar-title>
              <h4>Individual connections of developers on artifacts</h4>
            </v-toolbar-title>
          </v-toolbar>
          <v-divider></v-divider>
          <v-card-text class="pa-0">
            <template>
              <span>
                <br />&nbsp;This can indicate how much the developer has worked
                on a particular artifact. It may be possible to identify, for
                example, if that developer is concentrating knowledge of a
                certain region of the code or if he works with only a certain
                programming language. &nbsp;
                <br />
              </span>

              <span>
                <br />
                <b>&nbsp; Other notes</b> (This helps to mitigate the risk of
                developers distorting their work to fit the metrics):
                <br />
                <span
                  >&nbsp;- When observing LOC (project and developers), consider
                  that the team must follow the appropriate code standards of
                  the programming language (e.g. placement of "{}"). Also
                  consider defining a code review process, so that other
                  developers can evaluate the solutions implemented by a team
                  member, avoiding inappropriate or excessive LOC
                  solutions.</span
                >
                <br />
                <span
                  >&nbsp;- When observing commmits, consider that the team must
                  follow a commits pattern (e.g. as atomic as possible).</span
                >
                <br />
              </span>
            </template>
          </v-card-text>
        </v-card>
      </v-menu>
    </div>
    <!-- 6. recomendations developers -->
    <div
      class="text-center"
      style="float: left"
      v-if="
        perspective === 'Explore' &&
        projectVersions.projectVersion1.developerList.length > 0
      "
    >
      <v-menu open-on-hover right offset-y>
        <template v-slot:activator="{ on }">
          <v-icon
            right
            v-on="on"
            color="orange"
            class="fa-blink"
            style="position: absolute; top: 400px; font-size: 25px; left: -5px"
            >assistant</v-icon
          >
        </template>

        <v-card>
          <v-toolbar card dense color="transparent">
            <v-toolbar-title>
              <h4>Joint connections of developers on artifacts</h4>
            </v-toolbar-title>
          </v-toolbar>
          <v-divider></v-divider>
          <v-card-text class="pa-0">
            <template>
              <span>
                <br />&nbsp;This can indicate how hard the developers worked on
                a particular artifact. It may be possible, for example, to
                identify the distribution of knowledge in the artifact and the
                demand for change. &nbsp;
                <br />
              </span>

              <span>
                <br />
                <b>&nbsp; Other notes</b> (This helps to mitigate the risk of
                developers distorting their work to fit the metrics):
                <br />
                <span
                  >&nbsp;- When observing LOC (project and developers), consider
                  that the team must follow the appropriate code standards of
                  the programming language (e.g. placement of "{}"). Also
                  consider defining a code review process, so that other
                  developers can evaluate the solutions implemented by a team
                  member, avoiding inappropriate or excessive LOC
                  solutions.</span
                >
                <br />
                <span
                  >&nbsp;- When observing commmits, consider that the team must
                  follow a commits pattern (e.g. as atomic as possible).</span
                >
                <br />
              </span>
            </template>
          </v-card-text>
        </v-card>
      </v-menu>
    </div>
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
    chart: ECharts,
  },
  data: () => ({
    secondLoading: false,
    perspective: "Overview",
    devTFListV1: [],
    devTFListV2: [],
    util: Util,
    history: {
      paths: [],
    },
    pie1: pie,
    pie2: pie,
    explore1: null,
    explore2: null,
    optionsChartProgrammingLanguage1: null,
    optionsChartProgrammingLanguage2: null,
    initOptions: {
      renderer: "canvas",
    },
    projectVersions: {
      projectVersion1: {
        numLoc: 0,
        numCommits: 0,
        firstCommit: "",
        LastCommit: "",
        numLocProgrammingLanguageList: [],
        developerList: [],
        localRepository: "",
      },
      projectVersion2: {
        numLoc: 0,
        numCommits: 0,
        firstCommit: "",
        LastCommit: "",
        numLocProgrammingLanguageList: [],
        developerList: [],
        localRepository: "",
      },
    },
    colors: [],
    headers: [
      {
        text: "Truck Factor", // avatar
        value: "truckFactor",
      },
      { text: "Name (email)", value: "name" },
      // { text: "NLOC", value: "numLoc" }
    ],
  }),
  methods: {
    setProject1(projectVersion1) {
      this.pie1.dataset = {};
      this.pie1.dataset.source = projectVersion1.numLocProgrammingLanguageList;
      this.optionsChartProgrammingLanguage1 = { ...pie };

      this.devTFListV1 = [];
      this.projectVersions.projectVersion1.developerList.forEach(
        (developer) => {
          this.devTFListV1.push(developer);
        }
      );
    },
    setProject2(projectVersion2) {
      this.pie2.dataset = {};
      this.pie2.dataset.source = projectVersion2.numLocProgrammingLanguageList;
      this.optionsChartProgrammingLanguage2 = { ...pie };

      this.devTFListV2 = [];
      this.projectVersions.projectVersion2.developerList.forEach(
        (developer) => {
          this.devTFListV2.push(developer);
        }
      );
    },

    setProject(newProjectVersions) {
      this.projectVersions.projectVersion1 = newProjectVersions.projectVersion1;
      this.projectVersions.projectVersion2 =
        newProjectVersions.projectVersion2 === null
          ? {
              numLoc: 0,
              numCommits: 0,
              firstCommit: "",
              LastCommit: "",
              numLocProgrammingLanguageList: [],
              developerList: [],
              localRepository: "",
            }
          : newProjectVersions.projectVersion2;
      this.setProject1(this.projectVersions.projectVersion1);
      if (
        newProjectVersions.projectVersion2 &&
        newProjectVersions.projectVersion2.localRepository
      )
        this.setProject2(this.projectVersions.projectVersion2);
    },
    buildExplore(nodeData) {
      window.getApp.$emit("START_LOADING");
      let filter = {
        directory: "",
        localRepository: this.projectVersions.projectVersion1.localRepository,
        remoteRepository: this.projectVersions.projectVersion1.remoteRepository,
        zoomPath: nodeData === null ? "Root" : nodeData.descrition,
        checkout1: this.projectVersions.projectVersion1.checkout,
        checkout2: this.projectVersions.projectVersion2.checkout,
        devTFListV1: this.devTFListV1,
        devTFListV2: this.devTFListV2,
      };

      if (nodeData !== null) this.updateHistory(nodeData);
      getExploreProject(filter)
        .then(
          (response) => {
            this.explore1 = this.setExplore(response.data.explore1);
            if (response.data.explore2)
              this.explore2 = this.setExplore(response.data.explore2);
            window.getApp.$emit("STOP_LOADING");
            this.secondLoading = false;
          },
          (error) => {
            alert("Erro: " + error);
            window.getApp.$emit("STOP_LOADING");
            this.secondLoading = false;
          }
        )
        .catch(function (error) {
          window.getApp.$emit("STOP_LOADING");
          this.secondLoading = false;
        });
    },

    updateHistory(nodeData) {
      let newHistory = [];
      let exist = false;

      this.history.paths.forEach((item) => {
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
              explore.linkList[i].loc +
              " (" +
              explore.linkList[i].commits +
              ")",
          },
        };
        explore.linkList[i].lineStyle = {
          normal: {
            color: explore.linkList[i].color,
          },
        };
      }

      for (let j = 0; j < explore.nodeList.length; j++) {
        explore.nodeList[j].value = explore.nodeList[j].numLoc;
        explore.nodeList[j].itemStyle = {
          normal: {
            color: explore.nodeList[j].color,
          },
        };
      }

      return getExplore(explore.nodeList, explore.linkList);
    },
    firstLoadExplore() {
      this.history.paths = [];
      this.buildExplore(null);
    },
  },
  created() {
    let that = this;
    window.getApp.$on("UPDATE_PROJECT", (updated) => {
      if (updated.componentSubType && updated.componentSubType === "graph") {
        if (
          updated.data.nodeType === "Folder" ||
          updated.data.nodeType === "Project"
        ) {
          that.buildExplore(updated.data);
        }
      } else {
        that.setProject(updated);
        if (that.perspective === "Explore") {
          that.secondLoading = true;
          that.firstLoadExplore();
        }
      }
    });
    window.getApp.$on("APP_LEVEL_CHANGE", (index) => {
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
    perspective: function (val) {
      if (val === "Explore") {
        this.firstLoadExplore();
      }
    },
  },

  //   Recomendações

  // - Commits e LOC do Projeto: Utilize para compreender a dimensão do projeto. Serve como um parâmetro para analisar as conexões entre desenvolvedores e artefatos.
  // - Linguagens de Programação do projeto: Utilize para compreender a demanda de tencnologias no projeto. Considere as diferentes características das linguagens de programação ao analisar isso, algumas linguagens podem demandar mais LOC devido às características delas.
  // - Truck Factor do Projeto: O Truck Factor é calculado baseado no grau de autoria dos desenvolvedores nos arquivos projeto. Considere que os desenvolvedores do Truck Factor podem concentrar o conhecimento de mais da metade dos arquivos do projeto. Quanto menor o valor do Truck Factor maior é a concentração de conhecimento. Para mitigar a concentração de conhecimento considere incluir práticas como programação em par e rodagem de pessoas no código fonte.
  // - Desenvolvedores do Truck Factor: São os membros do time que podem possuir alto grau de autoria e conhecimento sobre a versão do projeto. Observe em quais regiões do código fonte podem ser aplicadas práticas como práticas como programação em par e rodagem de pessoas para distribuir a concentração de conhecimento.
  // - Conexões individuais de desenvolvedores em artefatos: Isso pode indicar o quanto o desenvolvedor trabalhou em determinado artefato. Pode ser possível identificar, por exemplo, se esse desenvolvedor está concentrando o conhecimento de determinada região do código ou se ele atua com apenas uma determinada linguagem de programação.
  // - Conexões conjuntas de desenvolvedores em artefatos: Isso pode indicar o quanto os desenvolvedores trabalharam em determinado artefato. Pode ser possível, por exemplo, identificar a distribuição de conhecimento no artefato e a demanda de alteração.

  // Outras observações (Isso ajuda a mitigar o risco dos desenvolvedores distorcerem seu trabalho para adequarem-se às  métrica):
  // - Ao observar LOC (projeto e desenvolvedores), considere que o time deve seguir os devidos padrões de código da linguagem de programação (e.g. posicionamento de "{}"). Considere também definir um processo de code review, para que outros desenvolvedores avaliem as soluções implementadas por um membro do time, evitando soluções inadequadas ou com excessivo LOC.
  // - Ao observar commmits, considere que o time deve seguir um padrão commits (e.g. o mais atômico possível).
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

@keyframes fa-blink {
  0% {
    -webkit-transform: rotate(0deg);
  }

  50% {
    -webkit-transform: rotate(35deg);
  }
}

.fa-blink {
  -webkit-animation: fa-blink 2.75s linear infinite;
  -moz-animation: fa-blink 2.75s linear infinite;
  -ms-animation: fa-blink 2.75s linear infinite;
  -o-animation: fa-blink 2.75s linear infinite;
  animation: fa-blink 2.75s linear infinite;
}

.v-dialog__content--active {
  background-color: #ffffffa8;
}
</style>
