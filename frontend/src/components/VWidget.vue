<template>
  <div id="v-widget">
    <v-card>
      <v-toolbar color="transparent" flat dense card v-if="enableHeader">
        <v-toolbar-title style="width:100%">
          <h4 v-if="title">{{title}}</h4>
          <div v-else>
            <v-chip outlined label @click="changeLevel('init')">Root/</v-chip>
            <v-chip
              outlined
              label
              v-if="title2.paths.length > 0"
              v-for="(item, index) in title2.paths"
              :key="item"
              :disabled="index === title2.paths.length - 1"
              @click="changeLevel(index)"
            >{{ index === 0 ? item.descrition : item.name }}/</v-chip>
            <div class="text-center" style="float:right">
              <v-menu open-on-hover bottom offset-y>
                <template v-slot:activator="{ on }">
                  <v-icon
                    right
                    v-on="on"
                    color="primary"
                    style="position: absolute; top: -35px; right: -10px;"
                  >info</v-icon>
                </template>

                <v-card>
                  <v-toolbar card dense color="transparent">
                    <v-toolbar-title>
                      <h4>Interactions</h4>
                    </v-toolbar-title>
                  </v-toolbar>
                  <v-divider></v-divider>
                  <v-card-text class="pa-0">
                    <template>
                      <span>
                        <b>&nbsp; - Artifact Zoom In:</b> click on an artifact &nbsp;
                      </span>
                      <br />
                      <span>
                        <b>&nbsp; - Artifact Zoom Out:</b> click on the zoom history item positioned on the graph toolbar &nbsp;
                      </span>
                      <br />
                      <span>
                        <b>&nbsp; - Filter:</b> hover an artifact, developer or connection &nbsp;
                      </span>
                      <br />
                      <span>
                        <b>&nbsp; - Move item:</b> click and drag an artifact or developer &nbsp;
                      </span>
                      <br />
                      <span>
                        <b>&nbsp; - Move graph:</b> click on a blank part and drag the graph &nbsp;
                      </span>
                      <br />
                      <span>
                        <b>&nbsp; - Graphical Zoom in/out:</b> use the mouse scroll on the graph &nbsp;
                      </span>
                      <br />
                    </template>
                  </v-card-text>
                </v-card>
              </v-menu>
            </div>
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
