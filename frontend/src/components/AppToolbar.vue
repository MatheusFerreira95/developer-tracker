<template>
  <v-toolbar color="primary" fixed dark app>
    <v-toolbar-side-icon @click.stop="handleDrawerToggle">
      <v-icon>menu</v-icon>
    </v-toolbar-side-icon>

    <v-spacer></v-spacer>

    <v-toolbar-title>
      <span>Developer Tracker</span>
    </v-toolbar-title>

    <v-spacer></v-spacer>

    <v-btn icon @click="handleFullScreen()">
      <v-icon>{{iconscreen}}</v-icon>
    </v-btn>
  </v-toolbar>
</template>

<script>
import Util from "@/util";

export default {
  name: "app-toolbar",

  data: () => ({
    iconscreen: "fullscreen"
  }),

  methods: {
    handleDrawerToggle() {
      window.getApp.$emit("APP_DRAWER_TOGGLED");
    },
    handleFullScreen() {
      this.iconscreen =
        this.iconscreen === "fullscreen" ? "fullscreen_exit" : "fullscreen";
      Util.toggleFullScreen();
    }
  }
};
</script>
