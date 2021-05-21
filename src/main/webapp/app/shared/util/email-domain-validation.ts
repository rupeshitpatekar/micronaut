const isEmailDomainValid = function (value) {
  return value.endsWith('@springernature.com') || value.endsWith('@nature.com') || value.endsWith('@macmillaneducation.com');
};

export const validateEmailDomain = (email: string, ctx, input, cb) => {
  if (!email || (email !== '' && !isEmailDomainValid(email))) {
    cb("Only '@spingernature.com , @nature.com ,  @macmillaneducation.com' allowed.");
    return;
  }
  cb(true);
};
