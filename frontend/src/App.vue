<template>
  <div id="app">
    <template v-if="!$route.meta.public">
      <v-app id="inspire" class="app">
        <!-- loading -->
        <v-dialog
          v-model="loading"
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
        <!-- drawer -->
        <app-drawer class="app--drawer"></app-drawer>
        <!-- toolbar -->
        <app-toolbar class="app--toolbar"></app-toolbar>
        <!-- pages -->
        <v-content class="backgroundPage">
          <div class="page-wrapper">
            <router-view></router-view>
          </div>
        </v-content>
      </v-app>
    </template>
  </div>
</template>
<script>
import AppDrawer from "@/components/AppDrawer";
import AppToolbar from "@/components/AppToolbar";
import AppEvents from "./event";
export default {
  components: {
    AppDrawer,
    AppToolbar,
  },
  data: () => ({
    loading: false,
  }),

  created() {
    AppEvents.forEach((item) => {
      this.$on(item.name, item.callback);
    });
    window.getApp = this;
  },
};
</script>


<style lang="stylus" scoped>
.page-wrapper {
  min-height: calc(100vh - 64px - 50px - 81px);
}

.v-dialog__content--active {
  background-color: #ffffffa8;
}

.backgroundPage {
  background-color: #efefef;
}
</style>
