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
            <div
              class="v-subheader theme--light"
              :class="{ 'primary--text': nameProject !== 'Repository' }"
            >{{nameProject}}</div>
            <v-text-field
              flat
              solo
              prepend-inner-icon="link"
              placeholder="Enter link to Git..."
              hide-details
              @keyup.enter="getProjectInformations"
              v-model="filter.remoteRepository"
            ></v-text-field>
          </v-card>
        </v-flex>

        <!-- Filtros -->
        <v-flex sm12>
          <v-card>
            <div class="v-subheader theme--light">Filters</div>
            <v-expansion-panel class="elevation-0">
              <!-- Bracnh filtro -->
              <v-expansion-panel-content>
                <template v-slot:header>
                  <div>Branch</div>
                </template>
                <v-card>
                  <v-card-text>In progress...</v-card-text>
                </v-card>
              </v-expansion-panel-content>

              <!-- Data filtro -->
              <v-expansion-panel-content>
                <template v-slot:header>
                  <div>Date Range</div>
                </template>
                <v-card>
                  <v-card-text>In progress...</v-card-text>
                </v-card>
              </v-expansion-panel-content>
            </v-expansion-panel>
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
      default: true
    }
  },
  data: () => ({
    mini: false,
    drawer: true,
    nameProject: "Repository",
    filter: {
      remoteRepository: "",
      localRepository: "",
      dateRange: "",
      directory: "",
      branch: ""
    },
    project: {
      numLoc: 0,
      numCommits: 0,
      numActiveDays: 0,
      firstCommit: "",
      LastCommit: "",
      numFileProgrammingLanguageList: [],
      developerList: [],
      localRepository: ""
    }
  }),
  created() {
    window.getApp.$on("APP_DRAWER_TOGGLED", () => {
      this.drawer = !this.drawer;
    });
  },
  methods: {
    getProjectInformations() {
      window.getApp.$emit("START_LOADING");
      //verificar perspectiva para saber qual rota chamar

      //se serviço retornar sucesso -> alterar nome label de projeto selecionado

      getProject(this.filter)
        .then(
          response => {
            this.project = response.data;
            this.filter.localRepository = this.project.localRepository;
            this.updateNameRepository();

            window.getApp.$emit("UPDATE_PROJECT", this.project);
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

    updateNameRepository() {
      this.nameProject = this.filter.remoteRepository.substring(
        this.filter.remoteRepository.lastIndexOf("/") + 1,
        this.filter.remoteRepository.lastIndexOf(".git")
      );
      if (!this.nameProject) this.nameProject = "Repository";
    }
  },
  watch: {
    "filter.remoteRepository": function(value) {
      this.filter.localRepository = "";
    }
  }
};
</script>


<style lang="stylus">
#appDrawer {
  overflow: hidden;

  .drawer-menu--scroll {
    height: calc(100vh - 48px);
    overflow: auto;
  }
}
</style>
