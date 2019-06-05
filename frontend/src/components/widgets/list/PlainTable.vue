<template>
  <v-card>
    <v-toolbar card dense color="transparent">
      <v-toolbar-title>
        <h4>Developers</h4>
      </v-toolbar-title>
    </v-toolbar>
    <v-divider></v-divider>
    <v-card-text class="pa-0">
      <template>
        <v-data-table
          :headers="headers"
          :items="developers"
          hide-actions
          class="elevation-0"
          :expand="expand"
          item-key="name"
        >
          <template slot="items" slot-scope="props">
            <tr class="click-pointer" @click="props.expanded = !props.expanded">
              <td>
                <v-avatar size="36px">
                  <img :src="props.item.avatar" :alt="props.item.username">
                </v-avatar>
              </td>
              <td>{{ props.item.name }}</td>
              <td class="text-xs-left">{{ props.item.commits }}</td>
              <td class="text-xs-left">{{ props.item.firstCommit }}</td>
              <td class="text-xs-left">{{ props.item.lastCommit }}</td>
            </tr>
          </template>
          <template v-slot:expand="props">
            <v-card flat>
              <v-card-text>Active Days: {{props.item.activeDays}}</v-card-text>
              <v-card-text>LOC: {{props.item.loc}}</v-card-text>
              <v-card-text>Commits per type file: {{props.item.fileCountCommits}}</v-card-text>
            </v-card>
          </template>
        </v-data-table>
      </template>
      <v-divider></v-divider>
    </v-card-text>
  </v-card>
</template>

<script>
export default {
  data() {
    return {
      headers: [
        {
          text: "",
          align: "center",
          sortable: false,
          value: "avatar"
        },
        {
          text: "Name",
          align: "left",
          value: "name"
        },
        { text: "Commits", value: "commits" },
        { text: "First Commit", value: "firstCommit" },
        { text: "Last Commit", value: "lastCommit" }
      ],
      developers: [
        {
          commits: "Dessie",
          avatar:
            "https://s3.amazonaws.com/uifaces/faces/twitter/ludwiczakpawel/128.jpg",
          firstCommit: "10/10/2017",
          name: "Jhon",
          lastCommit: "10/10/2018",
          activeDays: 10,
          loc: 90,
          fileCountCommits: "Java - 10%; JS - 90%"
        },
        {
          commits: "Dessie",
          avatar:
            "https://s3.amazonaws.com/uifaces/faces/twitter/ludwiczakpawel/128.jpg",
          firstCommit: "10/10/2017",
          name: "Snow",
          lastCommit: "10/10/2018",
          activeDays: 10,
          loc: 90,
          fileCountCommits: "Java - 10%; JS - 90%"
        },
        {
          commits: "Dessie",
          avatar:
            "https://s3.amazonaws.com/uifaces/faces/twitter/ludwiczakpawel/128.jpg",
          firstCommit: "10/10/2017",
          name: "Jhon Snow",
          lastCommit: "10/10/2018",
          activeDays: 10,
          loc: 90,
          fileCountCommits: "Java - 10%; JS - 90%"
        },
        {
          commits: "Dessie",
          avatar:
            "https://s3.amazonaws.com/uifaces/faces/twitter/ludwiczakpawel/128.jpg",
          firstCommit: "10/10/2017",
          name: "Jhon Snow",
          lastCommit: "10/10/2018",
          activeDays: 10,
          loc: 90,
          fileCountCommits: "Java - 10%; JS - 90%"
        },
        {
          commits: "Dessie",
          avatar:
            "https://s3.amazonaws.com/uifaces/faces/twitter/ludwiczakpawel/128.jpg",
          firstCommit: "10/10/2017",
          name: "Dessie Snow",
          lastCommit: "10/10/2018",
          activeDays: 10,
          loc: 90,
          fileCountCommits: "Java - 10%; JS - 90%"
        },
        {
          commits: "Dessie",
          avatar:
            "https://s3.amazonaws.com/uifaces/faces/twitter/ludwiczakpawel/128.jpg",
          firstCommit: "10/10/2017",
          name: "Dessie",
          lastCommit: "10/10/2018",
          activeDays: 10,
          loc: 90,
          fileCountCommits: "Java - 10%; JS - 90%"
        },
        {
          commits: "Dessie",
          avatar:
            "https://s3.amazonaws.com/uifaces/faces/twitter/ludwiczakpawel/128.jpg",
          firstCommit: "10/10/2017",
          name: "Jhon Dessie",
          lastCommit: "10/10/2018",
          activeDays: 10,
          loc: 90,
          fileCountCommits: "Java - 10%; JS - 90%"
        },
        {
          commits: "Dessie",
          avatar:
            "https://s3.amazonaws.com/uifaces/faces/twitter/ludwiczakpawel/128.jpg",
          firstCommit: "10/10/2017",
          name: "Ramires Snow",
          lastCommit: "10/10/2018",
          activeDays: 10,
          loc: 90,
          fileCountCommits: "Java - 10%; JS - 90%"
        }
      ]
    };
  }
};
</script>
<style lang="stylus" scoped>
.click-pointer {
  cursor: pointer;
}
</style>