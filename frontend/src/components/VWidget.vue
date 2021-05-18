<template>
  <div id="v-widget">
    <v-card>
      <v-toolbar color="transparent" flat dense card>
        <v-toolbar-title style="width: 100%">
          <h4 v-if="title">{{ title }}</h4>
          <div v-else>
            <label style="font-size: 12px; color: gray">Paths:</label>
            <v-chip outlined label @click="changeLevel('init')">Root/</v-chip>
            <v-chip
              outlined
              label
              v-if="title2.paths.length > 0"
              v-for="(item, index) in title2.paths"
              :key="item"
              :disabled="index === title2.paths.length - 1"
              @click="changeLevel(index)"
              >{{ index === 0 ? item.descrition : item.name }}/</v-chip
            >
          </div>
        </v-toolbar-title>
        <v-spacer></v-spacer>
        <slot name="widget-header-action"></slot>
      </v-toolbar>
      <v-divider v-if="enableHeader"></v-divider>
      <v-card-text :class="contentBg">
        <slot name="widget-content"></slot>
      </v-card-text>
    </v-card>
  </div>
</template>

<script>
export default {
  name: "v-widget",
  props: {
    title: {
      type: String,
    },
    enableHeader: {
      type: Boolean,
      default: true,
    },
    contentBg: {
      type: String,
      default: "white",
    },
    title2: {
      type: Object,
    },
  },

  data() {
    return {};
  },
  methods: {
    changeLevel(index) {
      if (index === "init") this.title2.paths = [];
      window.getApp.$emit("APP_LEVEL_CHANGE", index);
    },
  },
  computed: {},
};
</script>
