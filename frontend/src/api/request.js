import axios from 'axios';

const service = axios.create({
  baseURL: "http://localhost:8080/"
})

service.defaults.headers.post['Content-Type'] = 'application/json';

export default service
