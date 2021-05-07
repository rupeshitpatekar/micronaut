export interface IComment {
  id?: number;
  comment?: string;
  postTitle?: string;
  postId?: number;
}

export const defaultValue: Readonly<IComment> = {};
