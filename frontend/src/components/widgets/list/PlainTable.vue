<template>
  <v-card>
    <v-toolbar card dense color="transparent">
      <v-toolbar-title><h4>Developers</h4></v-toolbar-title>
      <v-spacer></v-spacer>
      <v-btn icon>
        <v-icon>more_vert</v-icon>
      </v-btn>
    </v-toolbar>
    <v-divider></v-divider>
    <v-card-text class="pa-0">
      <template>
        <v-data-table
          :headers="headers"
          :items="projects"
          hide-actions
          class="elevation-0"
        >
          <template slot="items" slot-scope="props">
            <td>
              <v-avatar size="36px">
                <img :src="props.item.avatar" :alt="props.item.username" />
              </v-avatar>
            </td>
            <td>{{ props.item.name }}</td>
            <td class="text-xs-left">{{ props.item.loc }}</td>
            <td class="text-xs-left">{{ props.item.commits }}</td>
            <td class="text-xs-left">{{ props.item.firstCommit }}</td>
            <td class="text-xs-left">{{ props.item.lastCommit }}</td>
          </template>
        </v-data-table>
      </template>
      <v-divider></v-divider>
    </v-card-text>
  </v-card>
</template>

<script>
import { Projects } from '@/api/project';
export default {
  data () {
    return {
      headers: [
        {
          text: '',
          align: 'center',
          sortable: false,
          value: 'avatar'
        },
        {
          text: 'Name',
          align: 'left',
          value: 'name'
        },
        { text: 'LOC', value: 'loc' },
        { text: 'Commits', value: 'commits' },
        { text: 'First Commit', value: 'firstCommit' },
        { text: 'Last Commit', value: 'lastCommit' }
      ],
    };
  },
  computed: {
    projects () {
      return Projects;
    }
  }
};
</script>
