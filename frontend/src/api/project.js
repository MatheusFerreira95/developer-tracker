import request from './request';

export function getProject(filter) {
  return request({
    url: '/project',
    method: 'post',
    data: filter
  })
}
export function getExploreProject(filter) {
  return request({
    url: '/project/explore',
    method: 'post',
    data: filter
  })
}
export function getRecomendationByFileExtension(filter) {
  return request({
    url: '/project/recomendationByFileExtension',
    method: 'post',
    data: filter
  })
}
