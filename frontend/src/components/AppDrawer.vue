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
              v-model="filter.repositryPath"
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
      repositryPath: ""
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
      let that = this;
      setTimeout(function() {
        window.getApp.$emit("STOP_LOADING");

        that.nameProject = that.filter.repositryPath.substring(
          that.filter.repositryPath.lastIndexOf("/") + 1,
          that.filter.repositryPath.lastIndexOf(".git")
        );

        window.getApp.$emit("UPDATE_PROJECT", { name: that.nameProject });
        if (!that.nameProject) that.nameProject = "Repository";
      }, 2000);
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
