export default [
  {
    name: 'START_LOADING',
    callback: function (e) {
      this.loading = true;
    }
  },
  {
    name: 'STOP_LOADING',
    callback: function (e) {
      this.loading = false;
    }
  }
];
