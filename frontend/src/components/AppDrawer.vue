<template>
  <v-navigation-drawer
    id="appDrawer"
    :mini-variant.sync="mini"
    fixed
    :dark="$vuetify.dark"
    app
    v-model="drawer"
    width="260"
  >
    <!-- Titulo -->
    <v-toolbar color="primary darken-1" dark>
      <v-icon dark right>build</v-icon>

      <v-toolbar-title>
        <span>Tools</span>
      </v-toolbar-title>
    </v-toolbar>

    <v-container grid-list-xl fluid>
      <v-layout row wrap>
        <!-- campo de busca -->
        <v-flex sm12>
          <v-card>
            <div class="v-subheader theme--light primary--text">
              {{ nameProject }}
            </div>
            <v-text-field
              flat
              solo
              prepend-inner-icon="link"
              placeholder="Enter link to Git..."
              hide-details
              @keyup.enter="getProjectInformations"
              v-model="filter.remoteRepository"
            ></v-text-field>
            <v-text-field
              flat
              solo
              prepend-inner-icon="person"
              placeholder="Enter username..."
              hide-details
              @keyup.enter="getProjectInformations"
              v-model="filter.user"
            ></v-text-field>
            <v-text-field
              flat
              solo
              prepend-inner-icon="vpn_key"
              placeholder="Enter password..."
              hide-details
              type="password"
              @keyup.enter="getProjectInformations"
              v-model="filter.password"
            ></v-text-field>
            <v-text-field
              flat
              solo
              placeholder="Tag, branch or commit..."
              hide-details
              @keyup.enter="getProjectInformations"
              v-model="filter.checkout1"
            >
              <div class="prependInput" slot="prepend">V1</div>
            </v-text-field>

            <v-text-field
              flat
              solo
              :disabled="!filter.checkout1.trim()"
              placeholder="Tag, branch or commit..."
              hide-details
              @keyup.enter="getProjectInformations"
              v-model="filter.checkout2"
            >
              <div class="prependInput" slot="prepend">V2</div>
            </v-text-field>
          </v-card>
        </v-flex>

        <!-- botão de aplicar -->
        <v-flex sm12>
          <v-btn color="primary" dark block @click="getProjectInformations">
            Apply
            <v-icon dark right>check</v-icon>
          </v-btn>
        </v-flex>
      </v-layout>
    </v-container>
  </v-navigation-drawer>
</template>
<script>
import { getProject } from "@/api/project";

export default {
  name: "app-drawer",
  props: {
    expanded: {
      type: Boolean,
      default: true,
    },
  },
  data: () => ({
    mini: false,
    drawer: true,
    nameProject: "Repository",
    filter: {
      remoteRepository: "",
      localRepository: "",
      zoomPath: "./",
      directory: "",
      user: "",
      password: "",
      checkout1: "",
      checkout2: "",
    },
    projectVersion1: {
      numLoc: 0,
      numCommits: 0,
      numActiveDays: 0,
      firstCommit: "",
      LastCommit: "",
      numLocProgrammingLanguageList: [],
      developerList: [],
      localRepository: "",
    },
  }),
  created() {
    window.getApp.$on("APP_DRAWER_TOGGLED", () => {
      this.drawer = !this.drawer;
    });
  },
  methods: {
    getProjectInformations() {
      window.getApp.$emit("START_LOADING");

      getProject(this.filter)
        .then(
          (response) => {
            this.projectVersion1 = response.data.projectVersion1;
            this.filter.localRepository = this.projectVersion1.localRepository;
            this.updateNameRepository();

            window.getApp.$emit("UPDATE_PROJECT", response.data);
            window.getApp.$emit("STOP_LOADING");
          },
          (error) => {
            alert("Erro: " + error);
            window.getApp.$emit("STOP_LOADING");
          }
        )
        .catch(function (error) {
          window.getApp.$emit("STOP_LOADING");
        });
    },

    updateNameRepository() {
      this.nameProject = this.filter.remoteRepository.substring(
        this.filter.remoteRepository.lastIndexOf("/") + 1,
        this.filter.remoteRepository.lastIndexOf(".git")
      );
      if (!this.nameProject) this.nameProject = "Repository";
    },
  },
  watch: {
    "filter.remoteRepository": function (value) {
      this.filter.localRepository = "";
    },
    "filter.checkout1": function (value) {
      if (!value.trim()) this.filter.checkout2 = "";
    },
  },
};
</script>

<style lang="stylus">
#appDrawer {
  overflow: hidden;

  .drawer-menu--scroll {
    height: calc(100vh - 48px);
    overflow: auto;
  }

  .prependInput {
    margin-left: 15px;
    margin-top: 5px;
    margin-right: -16px;
    z-index: 1;
    color: #777;
    font-weight: 900;
  }
}
</style>
