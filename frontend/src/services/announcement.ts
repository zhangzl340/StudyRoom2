import service from './axios'

// 获取公告列表
export async function getAnnouncementList(params?: any) {
  return service({
    url: '/announcement/list',
    method: 'get',
    params
  })
}

// 获取公告详情
export async function getAnnouncementDetail(announcementId: number) {
  return service({
    url: `/announcement/detail/${announcementId}`,
    method: 'get'
  })
}

// 创建公告
export async function createAnnouncement(announcementData: any) {
  return service({
    url: '/announcement/create',
    method: 'post',
    data: announcementData
  })
}

// 更新公告
export async function updateAnnouncement(announcementId: number, announcementData: any) {
  return service({
    url: `/announcement/update/${announcementId}`,
    method: 'put',
    data: announcementData
  })
}

// 删除公告
export async function deleteAnnouncement(announcementId: number) {
  return service({
    url: `/announcement/delete/${announcementId}`,
    method: 'delete'
  })
}

// 获取活跃的公告
export async function getActiveAnnouncements() {
  return service({
    url: '/announcement/active',
    method: 'get'
  })
}

// 更新公告状态
export async function updateAnnouncementStatus(announcementId: number, isActive: boolean) {
  return service({
    url: `/announcement/status/${announcementId}`,
    method: 'put',
    params: {
      isActive
    }
  })
}

// 获取最近的公告
export async function getRecentAnnouncements(limit?: number) {
  return service({
    url: '/announcement/recent',
    method: 'get',
    params: {
      limit: limit || 5
    }
  })
}
