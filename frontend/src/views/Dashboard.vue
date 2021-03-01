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
                <br />
                <span
                  >The "Tools" menu has the necessary tools for you to generate
                  your developer tracking views. You must enter the URL for a
                  GIT-based code repository that represents the software
                  project. If authentication is required to access the code
                  repository, please enter your authentication credentials. You
                  can select two different versions for comparison, but you can
                  also view a single version. If fields V1 and V2 are empty, the
                  view will be referring to the default branch of the code
                  repository (usually the master branch).</span
                >
                <br />
                <br />
                <v-btn color="secondary" @click="startButtonClick">
                  Click here to start!
                </v-btn>
              </div>
            </v-card-title>
          </v-card>
        </v-flex>
      </v-layout>

      <!-- tabs -->
      <v-layout row wrap v-if="projectVersions.projectVersion1.localRepository">
        <v-card style="margin: 0px 12px 12px 12px">
          <!-- <v-flex xs12 style="padding-top: 0"> -->
          <v-tabs
            v-model="active"
            icons-and-text
            centered
            slider-color="primary"
            active-class="active-tab"
          >
            <v-tab @click="perspective = 'Overview'">
              Project Perspective
              <v-icon>work</v-icon>
            </v-tab>
            <v-tab @click="perspective = 'Explore'">
              Developer Perspective
              <v-icon>device_hub</v-icon></v-tab
            >
          </v-tabs>
        </v-card>
        <!-- </v-flex> -->
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
          <v-flex xs12>
            <v-card>
              <v-toolbar card dense color="transparent">
                <v-toolbar-title>
                  <h4>
                    Commits:
                    <b class="primary--text">
                      {{ projectVersions.projectVersion1.numCommits }}
                    </b>
                  </h4>
                </v-toolbar-title>
              </v-toolbar>
            </v-card>
          </v-flex>
          <v-flex xs12>
            <v-card>
              <v-toolbar card dense color="transparent">
                <v-toolbar-title>
                  <h4>
                    Lines Of Code:
                    <b class="primary--text">{{
                      projectVersions.projectVersion1.numLoc
                    }}</b>
                  </h4>
                </v-toolbar-title>
              </v-toolbar>
            </v-card>
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
          <v-flex sm12 xs12>
            <v-widget title="Technology Domain" content-bg="white">
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
          <v-flex sm12 xs12>
            <v-card>
              <v-toolbar card dense color="transparent">
                <v-toolbar-title>
                  <h4>
                    Truck Factor:
                    <b class="primary--text">{{
                      projectVersions.projectVersion1.truckFactor
                    }}</b>
                  </h4>
                </v-toolbar-title>
                <v-divider class="white"></v-divider>
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
                  >
                    <template slot="items" slot-scope="props">
                      <tr>
                        <td class="text-xs-left">
                          {{ props.item.name }}
                        </td>
                        <td class="text-xs-left">
                          <!-- <v-icon dark medium :color="util.getColors()[props.item.avatar]">person</v-icon> -->
                          <div
                            v-if="props.item.truckFactor"
                            style="color: darkblue"
                          >
                            <b>Yes</b>
                          </div>
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
          <v-flex xs12 class="button-view-configuration">
            <v-menu
              key="showHide"
              rounded="true"
              offset-y
              :close-on-content-click="false"
              max-height="400px"
            >
              <template v-slot:activator="{ attrs, on }">
                <v-btn
                  color="primary"
                  class="white--text"
                  v-bind="attrs"
                  v-on="on"
                >
                  View Configuration
                  <v-icon dark right>settings</v-icon>
                </v-btn>
              </template>

              <v-card>
                <v-tabs vertical>
                  <v-tab>
                    <v-icon left> person </v-icon>
                    Developers
                  </v-tab>
                  <v-tab>
                    <v-icon left> folder </v-icon>
                    Artifacts
                  </v-tab>

                  <v-tab-item>
                    <v-card flat>
                      <v-card-text>
                        <div style="display: inline-flex">
                          <v-subheader class="theme--light blue--text"
                            ><span
                              style="cursor: pointer"
                              @click="showHideAll(false, 'Developer')"
                              >Show all</span
                            >&nbsp;/&nbsp;
                            <span
                              style="cursor: pointer"
                              @click="showHideAll(true, 'Developer')"
                              >Hide all</span
                            ></v-subheader
                          >
                          <v-text-field
                            filled
                            label="Filter developers list"
                            v-model="filterListDevelopers"
                          ></v-text-field>
                        </div>
                        <table
                          style="
                            background-color: white;
                            table-layout: fixed;
                            width: 450px;
                          "
                        >
                          <tr>
                            <th
                              style="
                                text-align: left;
                                center-align: top;
                                padding-top: 5px;
                                padding-left: 15px;
                              "
                            >
                              V1
                            </th>
                          </tr>
                          <tr>
                            <td
                              style="
                                text-align: left;
                                vertical-align: top;
                                padding: 0;
                              "
                            >
                              <!--devs-->
                              <ul style="list-style-type: none">
                                <li
                                  v-for="node in this.bkpExplore1.nodeList"
                                  :key="node.name"
                                  @click="showHideNode(node.name)"
                                  class="click-pointer"
                                >
                                  <div
                                    v-if="
                                      node.nodeType === 'Developer' &&
                                      (filterListDevelopers === '' ||
                                        node.name
                                          .toLowerCase()
                                          .includes(
                                            filterListDevelopers.toLowerCase()
                                          ))
                                    "
                                  >
                                    <v-icon small v-if="node.hide"
                                      >visibility_off</v-icon
                                    >
                                    <v-icon small v-else class="blue--text"
                                      >visibility</v-icon
                                    >
                                    {{ node.name }}
                                  </div>
                                </li>
                              </ul>
                            </td>
                          </tr>
                        </table>
                      </v-card-text>
                    </v-card>
                  </v-tab-item>
                  <v-tab-item>
                    <v-card flat>
                      <v-card-text>
                        <div style="display: inline-flex">
                          <v-subheader
                            class="theme--light blue--text"
                            style="cursor: pointer; margin-right: 5px"
                            ><span
                              style="cursor: pointer"
                              @click="showHideAll(false, 'Project,File,Folder')"
                              >Show all</span
                            >&nbsp;/&nbsp;
                            <span
                              style="cursor: pointer"
                              @click="showHideAll(true, 'Project,File,Folder')"
                              >Hide all</span
                            ></v-subheader
                          >
                          <v-text-field
                            filled
                            label="Filter artifacts list"
                            v-model="filterListArtifacts"
                          ></v-text-field>
                        </div>
                        <table
                          style="
                            background-color: white;
                            table-layout: fixed;
                            width: 450px;
                          "
                        >
                          <tr>
                            <th
                              style="
                                text-align: left;
                                center-align: top;
                                padding-top: 5px;
                                padding-left: 15px;
                              "
                            >
                              V1
                            </th>
                          </tr>
                          <tr>
                            <td
                              style="
                                text-align: left;
                                vertical-align: top;
                                padding: 0;
                              "
                            >
                              <!--artifacts-->
                              <ul style="list-style-type: none">
                                <li
                                  v-for="node in this.bkpExplore1.nodeList"
                                  :key="node.name"
                                  @click="showHideNode(node.name)"
                                  class="click-pointer"
                                >
                                  <div
                                    v-if="
                                      node.nodeType !== 'Developer' &&
                                      (filterListArtifacts === '' ||
                                        node.name
                                          .toLowerCase()
                                          .includes(
                                            filterListArtifacts.toLowerCase()
                                          )) &&
                                      (filterListArtifacts === '' ||
                                        node.name
                                          .toLowerCase()
                                          .includes(
                                            filterListArtifacts.toLowerCase()
                                          ))
                                    "
                                  >
                                    <v-icon small v-if="node.hide"
                                      >visibility_off</v-icon
                                    >
                                    <v-icon small v-else class="blue--text"
                                      >visibility</v-icon
                                    >
                                    {{ node.name }}
                                  </div>
                                </li>
                              </ul>
                            </td>
                          </tr>
                        </table>
                      </v-card-text>
                    </v-card>
                  </v-tab-item>
                </v-tabs>
              </v-card>
            </v-menu>
          </v-flex>

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
            <v-card>
              <v-toolbar card dense color="transparent">
                <v-toolbar-title>
                  <h4>
                    Commits:
                    <b class="primary--text">
                      {{ projectVersions.projectVersion1.numCommits }}
                    </b>
                  </h4>
                </v-toolbar-title>
              </v-toolbar>
            </v-card>
          </v-flex>
          <v-flex lg6 sm6 xs12>
            <v-card>
              <v-toolbar card dense color="transparent">
                <v-toolbar-title>
                  <h4>
                    Commits:
                    <b class="primary--text">
                      {{ projectVersions.projectVersion2.numCommits }}
                    </b>
                  </h4>
                </v-toolbar-title>
              </v-toolbar>
            </v-card>
          </v-flex>
          <v-flex lg6 sm6 xs12>
            <v-card>
              <v-toolbar card dense color="transparent">
                <v-toolbar-title>
                  <h4>
                    Lines Of Code:
                    <b class="primary--text">
                      {{ projectVersions.projectVersion1.numLoc }}</b
                    >
                  </h4>
                </v-toolbar-title>
              </v-toolbar>
            </v-card>
          </v-flex>
          <v-flex lg6 sm6 xs12>
            <v-card>
              <v-toolbar card dense color="transparent">
                <v-toolbar-title>
                  <h4>
                    Lines Of Code:
                    <b class="primary--text">
                      {{ projectVersions.projectVersion2.numLoc }}
                    </b>
                  </h4>
                </v-toolbar-title>
              </v-toolbar>
            </v-card>
          </v-flex>

          <!-- Grafico Linguagem de programacao-->
          <v-flex lg6 sm6 xs12>
            <v-widget title="Technology Domain" content-bg="white">
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
            <v-widget title="Technology Domain" content-bg="white">
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
                  <h4>
                    Truck Factor:
                    <b class="primary--text">{{
                      projectVersions.projectVersion1.truckFactor
                    }}</b>
                  </h4>
                </v-toolbar-title>
                <v-divider class="white"></v-divider>
              </v-toolbar>
              <v-divider></v-divider>
              <v-card-text class="pa-0">
                <template>
                  <v-data-table
                    :headers="headers"
                    :items="developersListComparative"
                    class="elevation-0"
                    hide-actions
                    item-key="name"
                  >
                    <template slot="items" slot-scope="props">
                      <tr>
                        <td class="text-xs-left">
                          {{ props.item.version === 2 ? "" : props.item.name }}
                        </td>
                        <td class="text-xs-left">
                          <!-- <v-icon dark medium :color="util.getColors()[props.item.avatar]">person</v-icon> -->
                          <div
                            v-if="props.item.truckFactor"
                            style="color: darkblue"
                          >
                            <b>{{ props.item.version === 2 ? "" : "Yes" }}</b>
                          </div>
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
                  <h4>
                    Truck Factor:
                    <b class="primary--text">{{
                      projectVersions.projectVersion2.truckFactor
                    }}</b>
                  </h4>
                </v-toolbar-title>
                <v-divider class="white"></v-divider>
              </v-toolbar>
              <v-divider></v-divider>
              <v-card-text class="pa-0">
                <template>
                  <v-data-table
                    :headers="headers"
                    :items="developersListComparative"
                    class="elevation-0"
                    hide-actions
                    item-key="name"
                  >
                    <template slot="items" slot-scope="props">
                      <tr>
                        <td class="text-xs-left">
                          {{ props.item.version === 1 ? "" : props.item.name }}
                        </td>
                        <td class="text-xs-left">
                          <!-- <v-icon dark medium :color="util.getColors()[props.item.avatar]">person</v-icon> -->
                          <div
                            v-if="props.item.truckFactor"
                            style="color: darkblue"
                          >
                            <b>{{ props.item.version === 1 ? "" : "Yes" }}</b>
                          </div>
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

        <!-- Explore 2 versoes: comparativo -->
        <template
          v-if="perspective === 'Explore' && bkpExplore2.nodeList && !showDiff"
        >
          <v-flex xs12 class="button-view-configuration">
            <v-menu
              key="showHide"
              rounded="true"
              offset-y
              :close-on-content-click="false"
              max-height="400px"
            >
              <template v-slot:activator="{ attrs, on }">
                <v-btn
                  color="primary"
                  class="white--text"
                  v-bind="attrs"
                  v-on="on"
                >
                  View Configuration
                  <v-icon dark right>settings</v-icon>
                </v-btn>
              </template>
              <div style="background-color: white">
                <br />
                <v-switch v-model="showDiff" label="Show Difference"></v-switch>
                <v-card>
                  <v-tabs vertical>
                    <v-tab>
                      <v-icon left> person </v-icon>
                      Developers
                    </v-tab>
                    <v-tab>
                      <v-icon left> folder </v-icon>
                      Artifacts
                    </v-tab>

                    <v-tab-item>
                      <v-card flat>
                        <v-card-text>
                          <div style="display: inline-flex">
                            <v-subheader class="theme--light blue--text"
                              ><span
                                style="cursor: pointer"
                                @click="showHideAll(false, 'Developer')"
                                >Show all</span
                              >&nbsp;/&nbsp;
                              <span
                                style="cursor: pointer"
                                @click="showHideAll(true, 'Developer')"
                                >Hide all</span
                              ></v-subheader
                            >

                            <v-text-field
                              filled
                              label="Filter developers list"
                              v-model="filterListDevelopers"
                            ></v-text-field>
                          </div>
                          <table
                            style="
                              background-color: white;
                              table-layout: fixed;
                              width: 450px;
                            "
                          >
                            <tr>
                              <th
                                style="
                                  text-align: left;
                                  center-align: top;
                                  padding-top: 5px;
                                  padding-left: 15px;
                                "
                              >
                                V1
                              </th>
                              <th
                                style="
                                  text-align: left;
                                  center-align: top;
                                  padding-top: 5px;
                                  padding-left: 15px;
                                "
                              >
                                V2
                              </th>
                            </tr>
                            <tr>
                              <td
                                style="
                                  text-align: left;
                                  vertical-align: top;
                                  padding: 0;
                                "
                              >
                                <!--devs-->
                                <ul style="list-style-type: none">
                                  <li
                                    v-for="node in this.bkpExplore1.nodeList"
                                    :key="node.name"
                                    @click="showHideNode(node.name)"
                                    class="click-pointer"
                                  >
                                    <div
                                      v-if="
                                        node.nodeType === 'Developer' &&
                                        (filterListDevelopers === '' ||
                                          node.name
                                            .toLowerCase()
                                            .includes(
                                              filterListDevelopers.toLowerCase()
                                            ))
                                      "
                                    >
                                      <v-icon small v-if="node.hide"
                                        >visibility_off</v-icon
                                      >
                                      <v-icon small v-else class="blue--text"
                                        >visibility</v-icon
                                      >
                                      {{ node.name }}
                                    </div>
                                  </li>
                                </ul>
                              </td>
                              <td
                                style="
                                  text-align: left;
                                  vertical-align: top;
                                  padding: 0;
                                "
                              >
                                <!--devs-->
                                <ul style="list-style-type: none">
                                  <li
                                    v-for="node in this.bkpExplore2.nodeList"
                                    :key="node.name"
                                    @click="showHideNode(node.name)"
                                    class="click-pointer"
                                  >
                                    <div
                                      v-if="
                                        node.nodeType === 'Developer' &&
                                        (filterListDevelopers === '' ||
                                          node.name
                                            .toLowerCase()
                                            .includes(
                                              filterListDevelopers.toLowerCase()
                                            ))
                                      "
                                    >
                                      <v-icon small v-if="node.hide"
                                        >visibility_off</v-icon
                                      >
                                      <v-icon small v-else class="blue--text"
                                        >visibility</v-icon
                                      >
                                      {{ node.name }}
                                    </div>
                                  </li>
                                </ul>
                              </td>
                            </tr>
                          </table>
                        </v-card-text>
                      </v-card>
                    </v-tab-item>
                    <v-tab-item>
                      <v-card flat>
                        <v-card-text>
                          <div style="display: inline-flex">
                            <v-subheader
                              class="theme--light blue--text"
                              style="cursor: pointer; margin-right: 5px"
                              ><span
                                style="cursor: pointer"
                                @click="
                                  showHideAll(false, 'Project,File,Folder')
                                "
                                >Show all</span
                              >&nbsp;/&nbsp;
                              <span
                                style="cursor: pointer"
                                @click="
                                  showHideAll(true, 'Project,File,Folder')
                                "
                                >Hide all</span
                              ></v-subheader
                            >
                            <v-text-field
                              filled
                              label="Filter artifacts list"
                              v-model="filterListArtifacts"
                            ></v-text-field>
                          </div>
                          <table
                            style="
                              background-color: white;
                              table-layout: fixed;
                              width: 450px;
                            "
                          >
                            <tr>
                              <th
                                style="
                                  text-align: left;
                                  center-align: top;
                                  padding-top: 5px;
                                  padding-left: 15px;
                                "
                              >
                                V1
                              </th>
                              <th
                                style="
                                  text-align: left;
                                  center-align: top;
                                  padding-top: 5px;
                                  padding-left: 15px;
                                "
                              >
                                V2
                              </th>
                            </tr>
                            <tr>
                              <td
                                style="
                                  text-align: left;
                                  vertical-align: top;
                                  padding: 0;
                                "
                              >
                                <!--artifacts-->
                                <ul style="list-style-type: none">
                                  <li
                                    v-for="node in this.bkpExplore1.nodeList"
                                    :key="node.name"
                                    @click="showHideNode(node.name)"
                                    class="click-pointer"
                                  >
                                    <div
                                      v-if="
                                        node.nodeType !== 'Developer' &&
                                        (filterListArtifacts === '' ||
                                          node.name
                                            .toLowerCase()
                                            .includes(
                                              filterListArtifacts.toLowerCase()
                                            ))
                                      "
                                    >
                                      <v-icon small v-if="node.hide"
                                        >visibility_off</v-icon
                                      >
                                      <v-icon small v-else class="blue--text"
                                        >visibility</v-icon
                                      >
                                      {{ node.name }}
                                    </div>
                                  </li>
                                </ul>
                              </td>
                              <td
                                style="
                                  text-align: left;
                                  vertical-align: top;
                                  padding: 0;
                                "
                              >
                                <!--artifacts-->
                                <ul style="list-style-type: none">
                                  <li
                                    v-for="node in this.bkpExplore2.nodeList"
                                    :key="node.name"
                                    @click="showHideNode(node.name)"
                                    class="click-pointer"
                                  >
                                    <div
                                      v-if="
                                        node.nodeType !== 'Developer' &&
                                        (filterListArtifacts === '' ||
                                          node.name
                                            .toLowerCase()
                                            .includes(
                                              filterListArtifacts.toLowerCase()
                                            ))
                                      "
                                    >
                                      <v-icon small v-if="node.hide"
                                        >visibility_off</v-icon
                                      >
                                      <v-icon small v-else class="blue--text"
                                        >visibility</v-icon
                                      >
                                      {{ node.name }}
                                    </div>
                                  </li>
                                </ul>
                              </td>
                            </tr>
                          </table>
                        </v-card-text>
                      </v-card>
                    </v-tab-item>
                  </v-tabs>
                </v-card>
              </div>
            </v-menu>
          </v-flex>
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
            <v-widget title content-bg="white" :title2="history">
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
        <!-- Explore diff 2 versoes: comparativo -->
        <template
          v-if="perspective === 'Explore' && bkpExplore2.nodeList && showDiff"
        >
          <v-flex xs12 class="button-view-configuration">
            <v-menu
              key="showHide"
              rounded="true"
              offset-y
              :close-on-content-click="false"
              max-height="400px"
            >
              <template v-slot:activator="{ attrs, on }">
                <v-btn
                  color="primary"
                  class="white--text"
                  v-bind="attrs"
                  v-on="on"
                >
                  View Configuration
                  <v-icon dark right>settings</v-icon>
                </v-btn>
              </template>
              <div style="background-color: white">
                <br />
                <v-switch v-model="showDiff" label="Show Difference"></v-switch>
                <v-card>
                  <v-tabs vertical>
                    <v-tab>
                      <v-icon left> person </v-icon>
                      Developers
                    </v-tab>
                    <v-tab>
                      <v-icon left> folder </v-icon>
                      Artifacts
                    </v-tab>

                    <v-tab-item>
                      <v-card flat>
                        <v-card-text>
                          <div style="display: inline-flex">
                            <v-subheader class="theme--light blue--text"
                              ><span
                                style="cursor: pointer"
                                @click="showHideAll(false, 'Developer')"
                                >Show all</span
                              >&nbsp;/&nbsp;
                              <span
                                style="cursor: pointer"
                                @click="showHideAll(true, 'Developer')"
                                >Hide all</span
                              ></v-subheader
                            >
                            <v-text-field
                              filled
                              label="Filter developers list"
                              v-model="filterListDevelopers"
                            ></v-text-field>
                          </div>
                          <table
                            style="
                              background-color: white;
                              table-layout: fixed;
                              width: 450px;
                            "
                          >
                            <tr>
                              <th
                                style="
                                  text-align: left;
                                  center-align: top;
                                  padding-top: 5px;
                                  padding-left: 15px;
                                "
                              >
                                V1
                              </th>
                            </tr>
                            <tr>
                              <td
                                style="
                                  text-align: left;
                                  vertical-align: top;
                                  padding: 0;
                                "
                              >
                                <!--devs-->
                                <ul style="list-style-type: none">
                                  <li
                                    v-for="node in this.bkpExploreDiff.nodeList"
                                    :key="node.name"
                                    @click="showHideNode(node.name)"
                                    class="click-pointer"
                                  >
                                    <div
                                      v-if="
                                        node.nodeType === 'Developer' &&
                                        (filterListDevelopers === '' ||
                                          node.name
                                            .toLowerCase()
                                            .includes(
                                              filterListDevelopers.toLowerCase()
                                            ))
                                      "
                                    >
                                      <v-icon small v-if="node.hide"
                                        >visibility_off</v-icon
                                      >
                                      <v-icon small v-else class="blue--text"
                                        >visibility</v-icon
                                      >
                                      {{ node.name }}
                                    </div>
                                  </li>
                                </ul>
                              </td>
                            </tr>
                          </table>
                        </v-card-text>
                      </v-card>
                    </v-tab-item>
                    <v-tab-item>
                      <v-card flat>
                        <v-card-text>
                          <div style="display: inline-flex">
                            <v-subheader
                              class="theme--light blue--text"
                              style="cursor: pointer; margin-right: 5px"
                              ><span
                                style="cursor: pointer"
                                @click="
                                  showHideAll(false, 'Project,File,Folder')
                                "
                                >Show all</span
                              >&nbsp;/&nbsp;
                              <span
                                style="cursor: pointer"
                                @click="
                                  showHideAll(true, 'Project,File,Folder')
                                "
                                >Hide all</span
                              ></v-subheader
                            >
                            <v-text-field
                              filled
                              label="Filter artifacts list"
                              v-model="filterListArtifacts"
                            ></v-text-field>
                          </div>
                          <table
                            style="
                              background-color: white;
                              table-layout: fixed;
                              width: 450px;
                            "
                          >
                            <tr>
                              <th
                                style="
                                  text-align: left;
                                  center-align: top;
                                  padding-top: 5px;
                                  padding-left: 15px;
                                "
                              >
                                V1
                              </th>
                            </tr>
                            <tr>
                              <td
                                style="
                                  text-align: left;
                                  vertical-align: top;
                                  padding: 0;
                                "
                              >
                                <!--artifacts-->
                                <ul style="list-style-type: none">
                                  <li
                                    v-for="node in this.bkpExploreDiff.nodeList"
                                    :key="node.name"
                                    @click="showHideNode(node.name)"
                                    class="click-pointer"
                                  >
                                    <div
                                      v-if="
                                        node.nodeType !== 'Developer' &&
                                        (filterListArtifacts === '' ||
                                          node.name
                                            .toLowerCase()
                                            .includes(
                                              filterListArtifacts.toLowerCase()
                                            )) &&
                                        (filterListArtifacts === '' ||
                                          node.name
                                            .toLowerCase()
                                            .includes(
                                              filterListArtifacts.toLowerCase()
                                            ))
                                      "
                                    >
                                      <v-icon small v-if="node.hide"
                                        >visibility_off</v-icon
                                      >
                                      <v-icon small v-else class="blue--text"
                                        >visibility</v-icon
                                      >
                                      {{ node.name }}
                                    </div>
                                  </li>
                                </ul>
                              </td>
                            </tr>
                          </table>
                        </v-card-text>
                      </v-card>
                    </v-tab-item>
                  </v-tabs>
                </v-card>
              </div>
            </v-menu>
          </v-flex>
          <v-flex lg2 sm2 xs12 style="z-index: 1; margin-right: -25px">
            <v-widget title="V1" content-bg="white">
              <div slot="widget-content">
                <span style="word-break: break-all">{{
                  projectVersions.projectVersion1.currentVersion
                }}</span>
                <br />
                <div
                  style="
                    font-size: 85px;
                    text-align: right;
                    color: gray;
                    background: white;
                    margin-right: -41px;
                    margin-bottom: -19px;
                    margin-left: -16px;
                  "
                >
                  &#8627;
                </div>
              </div>
            </v-widget>
          </v-flex>
          <v-flex lg8 sm8 xs12>
            <v-widget title content-bg="white" :title2="history">
              <div slot="widget-content">
                <chart
                  v-if="exploreDiff !== null"
                  :options="exploreDiff"
                  :init-options="initOptions"
                  ref="exploreDiff"
                  autoresize
                />
                <div v-else>Does not apply to this project</div>
              </div>
            </v-widget>
          </v-flex>
          <v-flex lg2 sm2 xs12 style="margin-left: -25px">
            <v-widget title="V2" content-bg="white">
              <div slot="widget-content">
                <span style="word-break: break-all">{{
                  projectVersions.projectVersion2.currentVersion
                }}</span>
                <br />
                <div
                  style="
                    font-size: 85px;
                    text-align: left;
                    color: gray;
                    transform: scale(1, -1);
                    background: white;
                    margin-bottom: -19px;
                    margin-left: -41px;
                    margin-right: -16px;
                  "
                >
                  &#8628;
                </div>
              </div></v-widget
            >
          </v-flex>
        </template>
      </v-layout>

      <!-- labels de versao -->
      <v-layout row wrap v-if="projectVersions.projectVersion1.localRepository">
        <v-flex v-if="!projectVersions.projectVersion2.currentVersion" xs12>
          <v-card class="cardVersionNumber">
            <div class="v-subheader theme--light versionNumberText">
              <span style="color: #000"
                >Project Version Reference Information: </span
              ><br />
              <span>{{ projectVersions.projectVersion1.currentVersion }}</span>
            </div>
          </v-card>
        </v-flex>
        <v-flex
          v-if="
            projectVersions.projectVersion2.currentVersion &&
            (perspective === 'Overview' || !showDiff)
          "
          lg6
          sm6
          xs12
        >
          <v-card class="cardVersionNumber">
            <div class="v-subheader theme--light versionNumberText">
              <span style="color: #000"
                >Project Version Reference Information (V1): </span
              ><br />
              <span>{{ projectVersions.projectVersion1.currentVersion }}</span>
            </div>
          </v-card>
        </v-flex>

        <v-flex
          v-if="
            projectVersions.projectVersion2.currentVersion &&
            (perspective === 'Overview' || !showDiff)
          "
          lg6
          sm6
          xs12
        >
          <v-card class="cardVersionNumber">
            <div class="v-subheader theme--light versionNumberText">
              <span style="color: #000"
                >Project Version Reference Information (V2): </span
              ><br />
              <span>{{ projectVersions.projectVersion2.currentVersion }}</span>
            </div>
          </v-card>
        </v-flex>
      </v-layout>
    </v-container>

    <!-- ícones do sistema -->
    <div>
      <v-menu open-on-hover left offset-x>
        <template v-slot:activator="{ on }">
          <v-chip
            v-on="on"
            style="z-index: 10; position: fixed; top: 12px; right: 70px"
            color="white"
            text-color="primary"
            >Help</v-chip
          >
        </template>

        <v-layout row wrap>
          <v-flex sm12>
            <v-card>
              <div class="v-subheader theme--light primary--text">
                Icons Legend
              </div>
              <div class="legend">
                <v-icon color="#B00020"> person </v-icon>Developer <br />

                <v-icon color="orange"> person </v-icon>Truck Factor Developer
                <br />

                <v-icon color="#FFD600"> folder </v-icon>Directory Artifact
                <br />

                <v-icon color="#1B5E20"> work </v-icon>Project Artifact
                <br />

                <v-icon color="#90A4AE"> insert_drive_file </v-icon>File
                Artifact <br />

                <v-icon color="#3f51b5"> trending_flat </v-icon
                >Artifact-Developer Relationship <br />

                <v-icon color="green"> trending_flat </v-icon>Artifact-Developer
                New Relationship <br />

                <v-icon color="red"> trending_flat </v-icon>Artifact-Developer
                Removed Relationship
                <br />
              </div>
            </v-card>
            <v-card>
              <div class="v-subheader theme--light primary--text">
                Interactions
              </div>
              <div class="legend">
                <span>
                  <b>&nbsp; - Artifact Zoom In:</b> click on an artifact &nbsp;
                </span>
                <br />
                <span>
                  <b>&nbsp; - Artifact Zoom Out:</b> click on the zoom history
                  item positioned on the graph toolbar &nbsp;
                </span>
                <br />
                <span>
                  <b>&nbsp; - Filter:</b> hover an artifact, developer or
                  connection &nbsp;
                </span>
                <br />
                <span>
                  <b>&nbsp; - Move item:</b> click and drag an artifact or
                  developer &nbsp;
                </span>
                <br />
                <span>
                  <b>&nbsp; - Move graph:</b> click on a blank part and drag the
                  graph &nbsp;
                </span>
                <br />
                <span>
                  <b>&nbsp; - Graphical Zoom in/out:</b> use the mouse scroll on
                  the graph &nbsp;
                </span>
                <br />
              </div>
            </v-card>
            <v-card>
              <div class="v-subheader theme--light primary--text">
                Get Started
              </div>
              <div class="legend">
                <span
                  >- The "Tools" menu has the necessary tools for you to
                  generate your developer tracking views. <br />- You must enter
                  the URL for a GIT-based code repository that represents the
                  software project. <br />- If authentication is required to
                  access the code repository, please enter your authentication
                  credentials.<br />
                  - You can select two different versions for comparison, but
                  you can also view a single version. <br />- If fields V1 and
                  V2 are empty, the view will be referring to the default branch
                  of the code repository (usually the master branch).</span
                >
              </div>
            </v-card>
          </v-flex>
        </v-layout>
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
    developersListComparative: [],
    filterListArtifacts: "",
    filterListDevelopers: "",
    showDiff: true,
    selecteds: [],
    secondLoading: false,
    perspective: "Overview",
    devTFListV1: [],
    devTFListV2: [],
    bkpExplore1: [],
    bkpExplore2: [],
    bkpExploreDiff: [],
    util: Util,
    history: {
      paths: [],
    },
    pie1: pie,
    pie2: pie,
    explore1: null,
    explore2: null,
    exploreDiff: null,
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
      { text: "Name", value: "name" },
      {
        text: "Included in Truck Factor", // avatar
        value: "truckFactor",
      },
      // { text: "NLOC", value: "numLoc" }
    ],
  }),
  methods: {
    showHideAll(showHide, nodeTypes) {
      this.bkpExplore1.nodeList.forEach((node) => {
        if (nodeTypes.includes(node.nodeType)) {
          node.hide = showHide;
        }
      });

      this.explore1 = getExplore(
        this.bkpExplore1.nodeList.filter(function (el) {
          return !el.hide;
        }),
        this.bkpExplore1.linkList.filter(function (el) {
          return !el.hide;
        })
      );

      if (this.bkpExplore2.length === 0) return;

      this.bkpExplore2.nodeList.forEach((node) => {
        if (nodeTypes.includes(node.nodeType)) {
          node.hide = showHide;
        }
      });

      this.explore2 = getExplore(
        this.bkpExplore2.nodeList.filter(function (el) {
          return !el.hide;
        }),
        this.bkpExplore2.linkList.filter(function (el) {
          return !el.hide;
        })
      );

      //bkpExplorediff
      this.bkpExploreDiff.nodeList.forEach((node) => {
        if (nodeTypes.includes(node.nodeType)) {
          node.hide = showHide;
        }
      });

      this.exploreDiff = getExplore(
        this.bkpExploreDiff.nodeList.filter(function (el) {
          return !el.hide;
        }),
        this.bkpExploreDiff.linkList.filter(function (el) {
          return !el.hide;
        })
      );
    },
    showHideNode(nodeName) {
      this.bkpExplore1.nodeList.forEach((node) => {
        if (node.name === nodeName) {
          node.hide = !node.hide;
        }
      });

      this.explore1 = getExplore(
        this.bkpExplore1.nodeList.filter(function (el) {
          return !el.hide;
        }),
        this.bkpExplore1.linkList.filter(function (el) {
          return !el.hide;
        })
      );

      if (this.bkpExplore2.length === 0) return;

      this.bkpExplore2.nodeList.forEach((node) => {
        if (node.name === nodeName) {
          node.hide = !node.hide;
        }
      });

      this.explore2 = getExplore(
        this.bkpExplore2.nodeList.filter(function (el) {
          return !el.hide;
        }),
        this.bkpExplore2.linkList.filter(function (el) {
          return !el.hide;
        })
      );

      //bkpExplorediff
      this.bkpExploreDiff.nodeList.forEach((node) => {
        if (node.name === nodeName) {
          node.hide = !node.hide;
        }
      });

      this.exploreDiff = getExplore(
        this.bkpExploreDiff.nodeList.filter(function (el) {
          return !el.hide;
        }),
        this.bkpExploreDiff.linkList.filter(function (el) {
          return !el.hide;
        })
      );
    },
    setProject1(projectVersion1) {
      projectVersion1.numLocProgrammingLanguageList.sort((a, b) =>
        a.nameProgrammingLanguage === "Others"
          ? 2
          : a.percentLOC < b.percentLOC
          ? 1
          : b.percentLOC < a.percentLOC
          ? -1
          : 0
      );

      this.pie1.dataset = {};
      this.pie1.dataset.source = projectVersion1.numLocProgrammingLanguageList;
      this.optionsChartProgrammingLanguage1 = { ...pie };

      this.devTFListV1 = [];
      this.projectVersions.projectVersion1.developerList.forEach(
        (developer) => {
          if (developer.truckFactor) this.devTFListV1.push(developer);
        }
      );
    },
    setProject2(projectVersion2) {
      projectVersion2.numLocProgrammingLanguageList.sort((a, b) =>
        a.nameProgrammingLanguage === "Others"
          ? 2
          : a.percentLOC < b.percentLOC
          ? 1
          : b.percentLOC < a.percentLOC
          ? -1
          : 0
      );

      this.pie2.dataset = {};
      this.pie2.dataset.source = projectVersion2.numLocProgrammingLanguageList;
      this.optionsChartProgrammingLanguage2 = { ...pie };

      this.devTFListV2 = [];
      this.projectVersions.projectVersion2.developerList.forEach(
        (developer) => {
          if (developer.truckFactor) this.devTFListV2.push(developer);
        }
      );

      //exibição lista devs
      this.developersListComparative = [];
      let devList1 =
        this.projectVersions.projectVersion1.developerList.length >=
        this.projectVersions.projectVersion2.developerList.length
          ? this.projectVersions.projectVersion1.developerList
          : this.projectVersions.projectVersion2.developerList;
      let devList2 =
        this.projectVersions.projectVersion1.developerList.length <
        this.projectVersions.projectVersion2.developerList.length
          ? this.projectVersions.projectVersion1.developerList
          : this.projectVersions.projectVersion2.developerList;

      for (let i = 0; i < devList1.length; i++) {
        let dev1 = devList1[i];
        let exist = "no";
        for (let j = 0; j < devList2.length; j++) {
          let dev2 = devList2[j];
          if (dev1.name === dev2.name) {
            exist = dev1.truckFactor === dev2.truckFactor ? "equal" : "diff";
            break;
          }
        }
        if (exist === "equal") {
          dev1.version = -1;
          this.developersListComparative.push(dev1);
        } else if (exist === "diff") {
          dev1.version =
            devList1 === this.projectVersions.projectVersion1.developerList
              ? 1
              : 2;
          this.developersListComparative.push(dev1);
          let dev2 = JSON.parse(JSON.stringify(dev1));
          dev2.version = dev1.version === 2 ? 1 : 2;
          dev2.truckFactor = !dev1.truckFactor;
          this.developersListComparative.push(dev2);
        } else {
          dev1.version =
            devList1 === this.projectVersions.projectVersion1.developerList
              ? 1
              : 2;
          this.developersListComparative.push(dev1);
        }
      }

      for (let i = 0; i < devList2.length; i++) {
        let dev1 = devList2[i];
        let exist = false;
        for (let j = 0; j < devList1.length; j++) {
          let dev2 = devList1[j];
          if (dev1.name === dev2.name) {
            exist = true;
            break;
          }
        }
        if (!exist) {
          dev1.version =
            devList2 === this.projectVersions.projectVersion1.developerList
              ? 1
              : 2;
          this.developersListComparative.push(dev1);
        }
      }
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
      let that = this;
      getExploreProject(filter)
        .then(
          (response) => {
            this.explore1 = this.setExplore(
              response.data.explore1,
              "bkpExplore1"
            );
            if (response.data.explore2)
              this.explore2 = this.setExplore(
                response.data.explore2,
                "bkpExplore2"
              );
            this.exploreDiff = this.buildExploreDiff();
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
          that.secondLoading = false;
          console.log(error);
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

    setExplore(explore, bkpExplore) {
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

      this[bkpExplore] = explore;

      let dynamicSort = function (property) {
        var sortOrder = 1;
        if (property[0] === "-") {
          sortOrder = -1;
          property = property.substr(1);
        }
        return function (a, b) {
          var result =
            a[property] < b[property] ? -1 : a[property] > b[property] ? 1 : 0;
          return result * sortOrder;
        };
      };

      this[bkpExplore].nodeList.sort(dynamicSort("name"));

      return getExplore(explore.nodeList, explore.linkList);
    },

    buildExploreDiff() {
      this.bkpExploreDiff = [];
      let exploreDiffTemp = { nodeList: [], linkList: [] };

      this.bkpExplore1.linkList.forEach((linkV1) => {
        let linkFound = null;
        linkFound = this.bkpExplore2.linkList.filter(function (linkV2) {
          return (
            linkV1.source === linkV2.source && linkV1.target === linkV2.target
          );
        });

        let linkDiff = JSON.parse(JSON.stringify(linkV1));
        linkDiff.commits = "";
        linkDiff.loc = "";

        if (linkFound[0]) {
          let linkV2 = linkFound[0];

          linkDiff.numLoc = linkV2.numLoc - linkV1.numLoc;
          linkDiff.value = linkDiff.numCommits =
            linkV2.numCommits - linkV1.numCommits;

          let difLocLabel =
            linkDiff.numLoc > 0 ? "+" + linkDiff.numLoc : linkDiff.numLoc;

          let difCommitLabel =
            linkDiff.numCommits > 0
              ? "+" + linkDiff.numCommits
              : linkDiff.numCommits;

          linkDiff.label.normal.formatter =
            linkDiff.numLoc === 0 && linkDiff.numCommits === 0
              ? "V1 = V2"
              : difLocLabel + "LOC in V2\n" + difCommitLabel + " Commit in V2";

          this.addNodesEndLinkToDiff(
            linkDiff,
            exploreDiffTemp,
            this.bkpExplore1.nodeList
          );
        } else {
          linkDiff.numLoc = 0;
          linkDiff.value = linkDiff.numCommits = 0;

          linkDiff.label.normal.formatter = "Removed in V2";

          linkDiff.lineStyle.normal.color = "red";

          this.addNodesEndLinkToDiff(
            linkDiff,
            exploreDiffTemp,
            this.bkpExplore1.nodeList
          );
        }
      });

      this.bkpExplore2.linkList.forEach((linkV2) => {
        let linkDiffFound = [];
        linkDiffFound = exploreDiffTemp.linkList.filter(function (linkDiff) {
          return (
            linkDiff.source === linkV2.source &&
            linkDiff.target === linkV2.target
          );
        });

        if (!linkDiffFound[0]) {
          let linkDiff = JSON.parse(JSON.stringify(linkV2));
          linkDiff.label.normal.formatter =
            "New in V2: " + linkDiff.label.normal.formatter;

          linkDiff.lineStyle.normal.color = "green";

          this.addNodesEndLinkToDiff(
            linkDiff,
            exploreDiffTemp,
            this.bkpExplore2.nodeList
          );
        }
      });

      this.bkpExploreDiff = exploreDiffTemp;

      let dynamicSort = function (property) {
        var sortOrder = 1;
        if (property[0] === "-") {
          sortOrder = -1;
          property = property.substr(1);
        }
        return function (a, b) {
          var result =
            a[property] < b[property] ? -1 : a[property] > b[property] ? 1 : 0;
          return result * sortOrder;
        };
      };

      this.bkpExploreDiff.nodeList.sort(dynamicSort("name"));

      return getExplore(exploreDiffTemp.nodeList, exploreDiffTemp.linkList);
    },

    addNodesEndLinkToDiff(linkDiff, exploreDiffTemp, listNodesRef) {
      exploreDiffTemp.linkList.push(linkDiff);

      let nodesToAdd = listNodesRef.filter(function (nodeV) {
        return nodeV.name === linkDiff.source || nodeV.name === linkDiff.target;
      });

      let nodesToAddNotRepeat = [];

      nodesToAdd.forEach((nodeAdd) => {
        let exist = false;
        exploreDiffTemp.nodeList.forEach((node) => {
          if (node.name === nodeAdd.name) exist = true;
        });

        if (!exist) {
          nodesToAddNotRepeat.push(JSON.parse(JSON.stringify(nodeAdd)));
        }
      });

      exploreDiffTemp.nodeList = exploreDiffTemp.nodeList.concat(
        nodesToAddNotRepeat
      );
    },

    firstLoadExplore() {
      this.history.paths = [];
      this.buildExplore(null);
    },
    startButtonClick() {
      window.getApp.$emit("APP_DRAWER_TOGGLED", true);
      window.linkRepository.focus();
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
// .v-tabs__bar theme--light {
// background-color: transparent;
// }

// .theme--light.v-tabs__bar {
// background-color: transparent;
// }

// .v-tabs__item {
// background-color: transparent;
// // border: 1px solid #bbb;
// }

// .transparent {
// background-color: transparent;
// }

// .v-tabs__container {
// border: 1px solid #bbb;
// border-bottom: 1px;
// }
.active-tab {
  color: #4056b5 !important;
  opacity: 1 !important;

  .v-icon {
    color: #4056b5 !important;
  }
}

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

.cardVersionNumber {
  word-break: break-all;
  text-align: center !important;

  .versionNumberText {
    font-size: 13px;
    display: block;
    text-align: center !important;
    font-weight: normal !important;
    padding-top: 4px;
  }
}

.button-view-configuration {
  text-align: right;
  padding: 0 !important;
  padding-right: 16px !important;
  margin-top: -72px;
  margin-bottom: -10px;
}

.legend {
  padding: 10px;
  padding-top: 0px;
  padding-right: 2px;
  font-size: 13px;
}
</style>
