export interface IAttachment {
  id?: number;
  fileName?: string;
  contentContentType?: string;
  content?: any;
  postId?: number;
}

export const defaultValue: Readonly<IAttachment> = {};
