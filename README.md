# team-tracker

Este projeto está sendo desenvolvido como ferramenta computacional para apoiar uma abordagem criada para auxiliar Gerentes de Projeto a avaliarem as contribuições de desenvolvedores em projetos de software.

### Arquitetura

A definiçao da arquitetura utilizada foi sugerida por **https://github.com/jonashackt/spring-boot-vuejs**

A estrutura de arquivos é a seguinte:

```
team-tracker
├─┬ backend     → módulo backend com o código Spring Boot
│ ├── src
│ └── pom.xml
├─┬ frontend    → módulo frontend com o código Vue.js
│ ├── src
│ └── pom.xml
└── pom.xml     → Maven que gerencia ambos os módulos

```

### Como usar?

#### Pré-requisitos

MacOSX
```
brew install node
npm install -g @vue/cli
```
Linux

```
sudo apt update
sudo apt install node
npm install -g @vue/cli
```
Windows

```
choco install npm
npm install -g @vue/cli
```

#### Comandos

| Command | Description |
| ------- | ----------- |
| `mvn clean install` | Executar no diretório `raiz` para atualizar dependências dos módulos frontend e backend |
| `npm run build` | Executar no diretório `frontend` para compilar o módulo Vue.js e enviá-lo para o projeto backend |
| `mvn --projects backend spring-boot:run` | Executar no diretório `raiz` para executar a aplicação em http://localhost:8088/ |
