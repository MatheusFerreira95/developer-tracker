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

      <v-toolbar-title> Tools </v-toolbar-title>
    </v-toolbar>

    <v-container grid-list-xl fluid>
      <v-layout row wrap>
        <!-- campo de busca -->
        <v-flex sm12>
          <v-card>
            <div class="v-subheader theme--light primary--text">
              {{ nameProject }}
            </div>
            <v-text-field
              flat
              solo
              prepend-inner-icon="link"
              placeholder="Enter link to Git..."
              hide-details
              ref="linkRepository"
              @keyup.enter="getProjectInformations"
              v-model="filter.remoteRepository"
            ></v-text-field>
            <v-text-field
              flat
              solo
              prepend-inner-icon="person"
              placeholder="Enter username..."
              hide-details
              @keyup.enter="getProjectInformations"
              v-model="filter.user"
            ></v-text-field>
            <v-text-field
              flat
              solo
              prepend-inner-icon="vpn_key"
              placeholder="Enter password..."
              hide-details
              type="password"
              @keyup.enter="getProjectInformations"
              v-model="filter.password"
            ></v-text-field>
            <v-text-field
              flat
              solo
              placeholder="Tag, branch or commit..."
              hide-details
              @keyup.enter="getProjectInformations"
              v-model="filter.checkout1"
            >
              <div class="prependInput" slot="prepend">V1</div>
            </v-text-field>

            <v-text-field
              flat
              solo
              :disabled="!filter.checkout1.trim()"
              placeholder="Tag, branch or commit..."
              hide-details
              @keyup.enter="getProjectInformations"
              v-model="filter.checkout2"
            >
              <div class="prependInput" slot="prepend">V2</div>
            </v-text-field>
          </v-card>
        </v-flex>

        <!-- botões -->
        <v-flex sm12>
          <v-btn color="primary" dark block @click="getProjectInformations">
            Apply
            <v-icon dark right>check</v-icon>
          </v-btn>
          <v-btn
            v-if="showClear"
            color="white"
            light
            block
            @click="clearButtonClick"
          >
            Clear
            <v-icon light right>backspace</v-icon>
          </v-btn>
          <v-btn
            v-if="showClear"
            color="secondary"
            light
            block
            @click="exportRecommendations"
          >
            Recommendations
            <v-icon light right> description </v-icon>
          </v-btn>
        </v-flex>
      </v-layout>
    </v-container>
  </v-navigation-drawer>
</template>
<script>
import { getProject } from "@/api/project";
import { jsPDF } from "jspdf";

export default {
  name: "app-drawer",
  props: {
    expanded: {
      type: Boolean,
      default: true,
    },
  },
  data: () => ({
    mini: false,
    drawer: false,
    showClear: false,
    nameProject: "Repository",
    filter: {
      remoteRepository: "",
      localRepository: "",
      zoomPath: "./",
      directory: "",
      user: "",
      password: "",
      checkout1: "",
      checkout2: "",
    },
    projectVersion1: {
      numLoc: 0,
      numCommits: 0,
      numActiveDays: 0,
      firstCommit: "",
      LastCommit: "",
      numLocProgrammingLanguageList: [],
      developerList: [],
      localRepository: "",
    },
    projectVersions: {},
  }),
  created() {
    window.getApp.$on("APP_DRAWER_TOGGLED", (status) => {
      this.drawer = status !== undefined ? status : !this.drawer;
    });
  },
  mounted() {
    window.linkRepository = this.$refs.linkRepository;
  },
  methods: {
    clearButtonClick() {
      document.location.reload(true);
    },
    getProjectInformations() {
      if (
        !this.filter.remoteRepository ||
        !this.filter.remoteRepository.trim()
      ) {
        alert("Please, Enter Link to Git!");
        return;
      }

      window.getApp.$emit("START_LOADING");

      getProject(this.filter)
        .then(
          (response) => {
            this.showClear = true;
            this.projectVersions = response.data;
            this.projectVersion1 = response.data.projectVersion1;
            this.filter.localRepository = this.projectVersion1.localRepository;
            this.updateNameRepository();

            window.getApp.$emit("UPDATE_PROJECT", response.data);
            window.getApp.$emit("STOP_LOADING");
          },
          (error) => {
            alert("Erro: " + error);
            window.getApp.$emit("STOP_LOADING");
          }
        )
        .catch(function (error) {
          window.getApp.$emit("STOP_LOADING");
        });
    },

    updateNameRepository() {
      this.nameProject = this.filter.remoteRepository.substring(
        this.filter.remoteRepository.lastIndexOf("/") + 1,
        this.filter.remoteRepository.lastIndexOf(".git")
      );
      if (!this.nameProject) this.nameProject = "Repository";
    },

    exportRecommendations() {
      var RecommendationReport = document.createElement("div");
      RecommendationReport.setAttribute("id", "RecommendationReport");
      let countElement = 0;

      let diagnosis1V1 =
        "<p> <b> Diagnosis &nbsp;" +
        ++countElement +
        ": </b> <br> DevTracker has detected that version <b> " +
        this.projectVersions.projectVersion1.currentVersion +
        "&nbsp;(V1) </b> has the effort of a team of <b> " +
        this.projectVersions.projectVersion1.developerList.length +
        " developers</b>. In addition, <b> " +
        this.getTruckFactorNames(
          this.projectVersions.projectVersion1.developerList
        ) +
        " </b>&nbsp;concentrate knowledge on 50% or more of version artifacts. ";

      let diagnosis1V2 = this.projectVersions.projectVersion2
        ? "&nbsp; Regarding version <b> " +
          this.projectVersions.projectVersion2.currentVersion +
          "&nbsp;(V2),</b> DevTracker has detected that it has has the effort of a team of <b> " +
          this.projectVersions.projectVersion2.developerList.length +
          " developers </b>. In addition, <b> " +
          this.getTruckFactorNames(
            this.projectVersions.projectVersion2.developerList
          ) +
          " </b>&nbsp; concentrate knowledge on 50% or more of version artifacts. "
        : "</p>";

      let Recommendation1 =
        "<p> <b> Recommendation &nbsp;" +
        countElement +
        ": </b> <br> When a small number of people on the team concentrate the knowledge about the implementation there is a risk of dependence on these people in the project. Investigate in which regions of the source code practices such as programming in peer and turnover of people to distribute knowledge among everyone on the team in a more homogeneous way. </p> ";

      /** TRADUÇÃO 1
 
      let diagnosis1V1 =
        "<p><b>Diagnóstico &nbsp;" +
        ++countElement +
        ":</b><br>DevTracker detectou que a versão <b>111</b> possui esforço do time composto por <b>222 desenvolvedores</b>. Além disso, os desenvolvedores <b>333,444...</b><b>concentram o conhecimento sobre 50% ou mais</b> dos artefatos da versão 111.";

      let diagnosis1V2 = !this.projectVersion1.projectVersion2
        ? "&nbsp;Com relação à versão <b>111</b>, DevTracker detectou que ela possui esforço do time composto por <b>222 desenvolvedores</b>. Além disso, os desenvolvedores <b>333,444...</b><b>concentram o conhecimento sobre 50% ou mais</b> dos artefatos da versão 111. </p>"
        : "</p>";

      let Recommendation1 =
        "<p><b>Recomendação &nbsp; " +
        countElement +
        ":</b><br>Quando uma pequena parcela de pessoas do time concentram o conhecimento sobre a implementação há um risco de dependência dessas pessoas no projeto. Investigue em quais regiões do código fonte podem ser aplicadas práticas como  programação em par e rotatividade de pessoas para distribuir o conhecimento entre todos do time de uma forma mais homogênea.</p>";
      */

      let diagnosis2V1 =
        "<p><b>Diagnosis &nbsp;" +
        ++countElement +
        ":</b><br>DevTracker has detected that version <b>" +
        this.projectVersions.projectVersion1.currentVersion +
        "&nbsp;(V1)</b> was built using the following technologies (proportionally): <b> " +
        this.getTechList(
          this.projectVersions.projectVersion1.numLocProgrammingLanguageList
        ) +
        "</b>. ";

      let diagnosis2V2 = this.projectVersions.projectVersion2
        ? "&nbsp; DevTracker has detected that version <b>" +
          this.projectVersions.projectVersion2.currentVersion +
          "&nbsp;(V2)</b> was built using the following technologies (proportionally): <b> " +
          this.getTechList(
            this.projectVersions.projectVersion2.numLocProgrammingLanguageList
          ) +
          "</b>. </p>"
        : "</p>";

      let recommendation2 =
        "<p><b>Recommendation &nbsp; " +
        countElement +
        ":</b><br> Note that there are some languages that require more effort (in LOC). Make sure the developers working on the software modules that use the programming languages that have the highest percentage of LOC as technology. It may be necessary to allocate other employees with the skill of this technology to the project. Also make sure that the technical capacity of the project team is consistent with the demand for programming languages. This can help you to optimize the allocation of people, avoiding underutilization and/or work overload for certain developers. </p>";

      /** TRADUÇÃO 2
 
      <p><b>Diagnóstico 6count:</b><br>
      DevTracker detectou que a versão <b>111</b><br>
      foi construída utilizando as seguintes tecnologias (proporcionalmente): <b>tec (%), ...</b><br>.</p>

      <p><b>Recomendação 6count:</b><br>
      Observe que há linguagens de promação que estão demandando maior esforço (em LOC). Certifique-se dos desenvolvedores que estão atuando nos módulos do software que utilizam como tecnologia as linguagens de programação que possuem maior porcentagem de LOC. Pode ser necessário alocar outros colaboradores com o skill dessa tecnologia para o projeto. Certifique-se também se a capacidade técnica do time do projeto está coerente com a demanda de linguagens de programação. Isso pode ajudá-lo a otimizar a alocação de pessoas, evitando a subutilização e/ou a sobrecarga de trabalho para determinados desenvolvedores.</p>
      */

      let diagnosis3V1 =
        "<p><b>Diagnosis &nbsp;" +
        ++countElement +
        ":</b><br>DevTracker has detected that version <b>" +
        this.projectVersions.projectVersion1.currentVersion +
        "&nbsp;(V1)</b> has the following characteristics: <br> <b> Commits:&nbsp;" +
        this.projectVersions.projectVersion1.numCommits +
        "<br> LOC:&nbsp;" +
        this.projectVersions.projectVersion1.numLoc +
        "</b>.<br> ";

      let diagnosis3V2 = this.projectVersions.projectVersion2
        ? "DevTracker has detected that version <b>" +
          this.projectVersions.projectVersion2.currentVersion +
          "&nbsp;(V2)</b> has the following characteristics: <br> <b> Commits:&nbsp;" +
          this.projectVersions.projectVersion2.numCommits +
          "<br> LOC:&nbsp;" +
          this.projectVersions.projectVersion2.numLoc +
          "</b>. </p>"
        : "</p>";

      let recommendation3 =
        "<p><b>Recommendation &nbsp; " +
        countElement +
        ":</b><br> Project Commits and LOC: Use to understand the dimension of the project. When looking at LOC (project and developer perspective), consider that the team must follow the appropriate code standards of the programming language (e.g. placement of {}}). Also consider defining a code review process, so that other developers can evaluate the solutions implemented by a team member, avoiding inappropriate or excessive LOC solutions. When observing commmits, consider that the team must follow a commits pattern (e.g. as atomic as possible). With this care you can now compare the difference in dimensions of the two versions. Also note if the technological demand and the performance of the developers was different in the two versions. These results can be indicative of the results of decisions taken throughout the project and the release of versions. </p>";

      /** TRADUÇÃO 3
       * 
      <p><b>Diagnóstico 7count:</b><br>
      DevTracker detectou que a versão <b>111</b><br>
      possui as seguintes características:<br>
      LOC <b>loc888</b><br>
      Commits <b>commits999</b><br>.</p>

      DevTracker detectou que a versão <b>111</b><br>
      possui as seguintes características:<br>
      LOC <b>loc888</b><br>
      Commits <b>commits999</b><br>.</p>


      <p><b>Recomendação 7count:</b><br>
      Commits e LOC do Projeto: Utilize para compreender a dimensão do projeto. Ao observar LOC (perspectiva projeto e desenvolvedores), considere que o time deve seguir os devidos padrões de código da linguagem de programação (e.g. posicionamento de "{}"). Considere também definir um processo de code review, para que outros desenvolvedores avaliem as soluções implementadas por um membro do time, evitando soluções inadequadas ou com excessivo LOC. Ao observar commmits, considere que o time deve seguir um padrão commits (e.g. o mais atômico possível). Com esses cuidados agora você pode comparar a diferença das dimensões das duas versões. Observe também se a demanda tecnlógica e a atuação dos desenvolvedores foi diferente nas duas versões. Esses resultados podem ser indicativos de resultados de decisões tomadas ao longo do projeto e do lançamento de versões.</p>
      */

      RecommendationReport.innerHTML =
        "<style> h10 { font-size: 15px } p { font-size: 7px;} </style>" +
        "<div style='width:400px; margin:20px; text-align: justify;'>" +
        "<h10>Developer Tracker App Recommendations</h10><br><br>" +
        diagnosis1V1 +
        diagnosis1V2 +
        Recommendation1 +
        "<hr><br>" +
        diagnosis2V1 +
        diagnosis2V2 +
        recommendation2 +
        "<hr><br>" +
        diagnosis3V1 +
        diagnosis3V2 +
        recommendation3 +
        "</div>";

      let doc2 = new jsPDF({ unit: "px" });
      doc2.html(RecommendationReport, {
        callback: function (doc2) {
          doc2.save("RecommendationReport.pdf");
        },
      });
    },

    getTruckFactorNames(developerList) {
      let names = "";

      developerList.forEach((dev) => {
        if (dev.truckFactor) {
          names += names.trim() ? ", " + dev.name : dev.name;
        }
      });

      return names;
    },

    getTechList(LPlist) {
      let techList = "";

      LPlist.forEach((dev) => {
        let stringFormatted =
          dev.nameProgrammingLanguage + " (" + dev.percentLOC + "%)";
        techList += techList.trim() ? ", " + stringFormatted : stringFormatted;
      });

      return techList;
    },
  },
  watch: {
    "filter.remoteRepository": function (value) {
      this.filter.localRepository = "";
    },
    "filter.checkout1": function (value) {
      if (!value.trim()) this.filter.checkout2 = "";
    },
  },
};
</script>

<style lang="stylus">
#appDrawer {
  overflow: hidden;
  background-color: #efefef;

  .drawer-menu--scroll {
    height: calc(100vh - 48px);
    overflow: auto;
  }

  .prependInput {
    margin-left: 15px;
    margin-top: 5px;
    margin-right: -16px;
    z-index: 1;
    color: #777;
    font-weight: 900;
  }
}
</style>
