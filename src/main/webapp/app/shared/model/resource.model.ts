export interface IResource {
  id?: number;
  fileName?: string;
  contentContentType?: string;
  content?: any;
  postId?: number;
}

export const defaultValue: Readonly<IResource> = {};
