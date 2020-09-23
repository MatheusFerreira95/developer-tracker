import axios from 'axios';

const service = axios.create({
  baseURL: "/api"
})

service.defaults.headers.post['Content-Type'] = 'application/json';

export default service
