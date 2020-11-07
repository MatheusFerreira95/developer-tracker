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
            @click="exportRecomendations"
          >
            Recomendations
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
            this.projectVersion1 = response.data.projectVersion1;
            this.filter.localRepository = this.projectVersion1.localRepository;
            this.updateNameRepository();

            this.createRecomendations(response.data);
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

    createRecomendations(project) {
      //devo persisitir os dados para geração do pdf em uma variavel no storage no navegador ou no document.window
    },

    exportRecomendations() {
      /*     let doc = new jsPDF();

      let tituloCommitLOC = "Project Commits and LOC";
      let conteudoCommitLOC =
        "Linguagens de Programação do projeto: Utilize para compreender a demanda de tencnologias no projeto. Considere as diferentes características das linguagens de programação ao analisar isso, algumas linguagens podem demandar mais LOC devido às características delas.";

      let tituloTechDomain = "Project Programming Languages";
      let conteudoTechDomain =
        "Linguagens de Programação do projeto: Utilize para compreender a demanda de tencnologias no projeto. Considere as diferentes características das linguagens de programação ao analisar isso, algumas linguagens podem demandar mais LOC devido às características delas.";

      let tituloTF = "Project Truck Factor";
      let conteudoTF =
        "Truck Factor do Projeto: O Truck Factor é calculado baseado no grau de autoria dos desenvolvedores nos arquivos projeto. Considere que os desenvolvedores do Truck Factor podem concentrar o conhecimento de mais da metade dos arquivos do projeto. Quanto menor o valor do Truck Factor maior é a concentração de conhecimento. Para mitigar a concentração de conhecimento considere incluir práticas como programação em par e rodagem de pessoas no código fonte. ";

      let tituloTVDev = "Truck Factor developers";
      let conteudoTVDev =
        "Desenvolvedores do Truck Factor: São os membros do time que podem possuir alto grau de autoria e conhecimento sobre a versão do projeto. Observe em quais regiões do código fonte podem ser aplicadas práticas como práticas como programação em par e rodagem de pessoas para distribuir a concentração de conhecimento.";

      let tituloConnection =
        "Individual connections of developers on artifacts";
      let conteudoConnection =
        "Conexões individuais de desenvolvedores em artefatos: Isso pode indicar o quanto o desenvolvedor trabalhou em determinado artefato. Pode ser possível identificar, por exemplo, se esse desenvolvedor está concentrando o conhecimento de determinada região do código ou se ele atua com apenas uma determinada linguagem de programação.";

      let tituloJoinConnection = "Joint connections of developers on artifacts";
      let conteudoJoinConnection =
        "Conexões conjuntas de desenvolvedores em artefatos: Isso pode indicar o quanto os desenvolvedores trabalharam em determinado artefato. Pode ser possível, por exemplo, identificar a distribuição de conhecimento no artefato e a demanda de alteração.";

      let tituloOthers = "Others";
      let conteudoOthers =
        'Outras observações (Isso ajuda a mitigar o risco dos desenvolvedores distorcerem seu trabalho para adequarem-se às  métrica): i) Ao observar LOC (projeto e desenvolvedores), considere que o time deve seguir os devidos padrões de código da linguagem de programação (e.g. posicionamento de "{}"). Considere também definir um processo de code review, para que outros desenvolvedores avaliem as soluções implementadas por um membro do time, evitando soluções inadequadas ou com excessivo LOC; ii) Ao observar commmits, considere que o time deve seguir um padrão commits (e.g. o mais atômico possível).';

      let content = [
        tituloCommitLOC,
        conteudoCommitLOC,
        " ",
        tituloTechDomain,
        conteudoTechDomain,
        " ",
        tituloTF,
        conteudoTF,
        " ",
        tituloTVDev,
        conteudoTVDev,
        " ",
        tituloConnection,
        conteudoConnection,
        " ",
        tituloJoinConnection,
        conteudoJoinConnection,
        " ",
        tituloOthers,
        conteudoOthers,
      ];
      doc.text(10, 20, "Recomendations");

      let position = 40;
      content.forEach((item) => {
        doc.text(10, position, item);
        position += 10;
      });

       doc.save("recomendations.pdf");
*/

      var recomendationReport = document.createElement("div");
      recomendationReport.setAttribute("id", "recomendationReport");

      let tituloCommitLOC = "<h10>Project Commits and LOC</h10>";
      let conteudoCommitLOC =
        "<p>Commits e LOC do Projeto: Utilize para compreender a dimensão do projeto. Serve como um parâmetro para analisar as conexões entre desenvolvedores e artefatos.</p>";

      let tituloTechDomain = "<h10>Project Programming Languages</h10>";
      let conteudoTechDomain =
        "<p>Linguagens de Programação do projeto: Utilize para compreender a demanda de tencnologias no projeto. Considere as diferentes características das linguagens de programação ao analisar isso, algumas linguagens podem demandar mais LOC devido às características delas.</p>";

      let tituloTF = "<h10>Project Truck Factor</h10>";
      let conteudoTF =
        "<p> Truck Factor do Projeto: O Truck Factor é calculado baseado no grau de autoria dos desenvolvedores nos arquivos projeto. Considere que os desenvolvedores do Truck Factor podem concentrar o conhecimento de mais da metade dos arquivos do projeto. Quanto menor o valor do Truck Factor maior é a concentração de conhecimento. Para mitigar a concentração de conhecimento considere incluir práticas como programação em par e rodagem de pessoas no código fonte. </p>";

      let tituloTVDev = "<h10>Truck Factor developers</h10>";
      let conteudoTVDev =
        "<p>Desenvolvedores do Truck Factor: São os membros do time que podem possuir alto grau de autoria e conhecimento sobre a versão do projeto. Observe em quais regiões do código fonte podem ser aplicadas práticas como práticas como programação em par e rodagem de pessoas para distribuir a concentração de conhecimento.</p>";

      let lineCut = "<hr>";

      let tituloConnection =
        "<h10>Individual connections of developers on artifacts</h10>";
      let conteudoConnection =
        "<p> Conexões individuais de desenvolvedores em artefatos: Isso pode indicar o quanto o desenvolvedor trabalhou em determinado artefato. Pode ser possível identificar, por exemplo, se esse desenvolvedor está concentrando o conhecimento de determinada região do código ou se ele atua com apenas uma determinada linguagem de programação.</p>";

      let tituloJoinConnection =
        "<h10>Joint connections of developers on artifacts</h10>";
      let conteudoJoinConnection =
        "<p> Conexões conjuntas de desenvolvedores em artefatos: Isso pode indicar o quanto os desenvolvedores trabalharam em determinado artefato. Pode ser possível, por exemplo, identificar a distribuição de conhecimento no artefato e a demanda de alteração.</p>";

      let tituloOthers = "<h10>Others</h10>";
      let conteudoOthers =
        '<p>Outras observações (Isso ajuda a mitigar o risco dos desenvolvedores distorcerem seu trabalho para adequarem-se às  métrica): i) Ao observar LOC (projeto e desenvolvedores), considere que o time deve seguir os devidos padrões de código da linguagem de programação (e.g. posicionamento de "{}"). Considere também definir um processo de code review, para que outros desenvolvedores avaliem as soluções implementadas por um membro do time, evitando soluções inadequadas ou com excessivo LOC; ii) Ao observar commmits, considere que o time deve seguir um padrão commits (e.g. o mais atômico possível).</p>';

      recomendationReport.innerHTML =
        "<style> h10 { font-size: 10px } p { font-size: 7px;} </style>" +
        "<div style='width:400px; margin:20px; text-align: justify;'>" +
        tituloCommitLOC +
        conteudoCommitLOC +
        tituloTechDomain +
        conteudoTechDomain +
        tituloTF +
        conteudoTF +
        tituloTVDev +
        conteudoTVDev +
        lineCut +
        tituloConnection +
        conteudoConnection +
        tituloJoinConnection +
        conteudoJoinConnection +
        tituloOthers +
        conteudoOthers +
        "</div>";

      try {
        document.body.appendChild(recomendationReport);
      } catch (e) {
        console.log(e);
      }
      // this.$htmlToPaper("recomendationReport");
      let doc2 = new jsPDF({ unit: "px" });
      doc2.html(recomendationReport, {
        callback: function (doc2) {
          doc2.save();
        },
      });
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
