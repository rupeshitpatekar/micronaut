import React, { useEffect } from 'react';
import { connect } from 'react-redux';
import { Link, RouteComponentProps } from 'react-router-dom';
import { Button, Row, Col } from 'reactstrap';
import { Translate, ICrudGetAction, openFile, byteSize } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { IRootState } from 'app/shared/reducers';
import { getEntity } from './attachment.reducer';
import { IAttachment } from 'app/shared/model/attachment.model';
import { APP_DATE_FORMAT, APP_LOCAL_DATE_FORMAT } from 'app/config/constants';

export interface IAttachmentDetailProps extends StateProps, DispatchProps, RouteComponentProps<{ id: string }> {}

export const AttachmentDetail = (props: IAttachmentDetailProps) => {
  useEffect(() => {
    props.getEntity(props.match.params.id);
  }, []);

  const { attachmentEntity } = props;
  return (
    <Row>
      <Col md="8">
        <h2>
          <Translate contentKey="sndealsApp.attachment.detail.title">Attachment</Translate> [<b>{attachmentEntity.id}</b>]
        </h2>
        <dl className="jh-entity-details">
          <dt>
            <span id="fileName">
              <Translate contentKey="sndealsApp.attachment.fileName">File Name</Translate>
            </span>
          </dt>
          <dd>{attachmentEntity.fileName}</dd>
          <dt>
            <span id="content">
              <Translate contentKey="sndealsApp.attachment.content">Content</Translate>
            </span>
          </dt>
          <dd>
            {attachmentEntity.content ? (
              <div>
                {attachmentEntity.contentContentType ? (
                  <a onClick={openFile(attachmentEntity.contentContentType, attachmentEntity.content)}>
                    <Translate contentKey="entity.action.open">Open</Translate>&nbsp;
                  </a>
                ) : null}
                <span>
                  {attachmentEntity.contentContentType}, {byteSize(attachmentEntity.content)}
                </span>
              </div>
            ) : null}
          </dd>
          <dt>
            <Translate contentKey="sndealsApp.attachment.post">Post</Translate>
          </dt>
          <dd>{attachmentEntity.postId ? attachmentEntity.postId : ''}</dd>
        </dl>
        <Button tag={Link} to="/attachment" replace color="info">
          <FontAwesomeIcon icon="arrow-left" />{' '}
          <span className="d-none d-md-inline">
            <Translate contentKey="entity.action.back">Back</Translate>
          </span>
        </Button>
        &nbsp;
        <Button tag={Link} to={`/attachment/${attachmentEntity.id}/edit`} replace color="primary">
          <FontAwesomeIcon icon="pencil-alt" />{' '}
          <span className="d-none d-md-inline">
            <Translate contentKey="entity.action.edit">Edit</Translate>
          </span>
        </Button>
      </Col>
    </Row>
  );
};

const mapStateToProps = ({ attachment }: IRootState) => ({
  attachmentEntity: attachment.entity,
});

const mapDispatchToProps = { getEntity };

type StateProps = ReturnType<typeof mapStateToProps>;
type DispatchProps = typeof mapDispatchToProps;

export default connect(mapStateToProps, mapDispatchToProps)(AttachmentDetail);
