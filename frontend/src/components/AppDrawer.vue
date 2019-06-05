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
            <div class="v-subheader theme--light">Repository</div>
            <v-text-field
              flat
              solo
              prepend-inner-icon="link"
              placeholder="Enter link to Git..."
              hide-details
              @keyup.enter="submit"
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
          <v-btn color="primary" dark block>
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
    drawer: true
  }),
  created() {
    window.getApp.$on("APP_DRAWER_TOGGLED", () => {
      this.drawer = !this.drawer;
    });
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
