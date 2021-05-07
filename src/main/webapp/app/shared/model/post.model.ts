export interface IPost {
  id?: number;
  title?: string;
  description?: string;
  location?: string;
  status?: string;
  categoryDisplayName?: string;
  categoryId?: number;
}

export const defaultValue: Readonly<IPost> = {};
