import request from './request';

export function getProject(filter) {
  return request({
    url: '/project',
    method: 'post',
    data: filter
  })
}
