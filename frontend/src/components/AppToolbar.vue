<template>
  <v-toolbar color="primary" fixed dark app>
    <v-toolbar-side-icon @click.stop="handleDrawerToggle">
      <v-icon>menu</v-icon>
    </v-toolbar-side-icon>

    <v-spacer></v-spacer>

    <v-toolbar-title>
      <span>Team Tracker</span>
    </v-toolbar-title>

    <v-spacer></v-spacer>

    <v-btn icon @click="handleFullScreen()">
      <v-icon>fullscreen</v-icon>
    </v-btn>

    <v-btn icon>
      <v-icon>settings</v-icon>
    </v-btn>
  </v-toolbar>
</template>

<script>
import Util from "@/util";

export default {
  name: "app-toolbar",

  data: () => ({}),

  methods: {
    handleDrawerToggle() {
      window.getApp.$emit("APP_DRAWER_TOGGLED");
    },
    handleFullScreen() {
      Util.toggleFullScreen();
    }
  }
};
</script>
