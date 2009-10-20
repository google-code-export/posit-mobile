<?php

/**
 * find form base class.
 *
 * @package    form
 * @subpackage find
 * @version    SVN: $Id: sfDoctrineFormGeneratedTemplate.php 8508 2008-04-17 17:39:15Z fabien $
 */
class BasefindForm extends BaseFormDoctrine
{
  public function setup()
  {
    $this->setWidgets(array(
      'id'           => new sfWidgetFormInputHidden(),
      'device_id'    => new sfWidgetFormDoctrineChoice(array('model' => 'Device', 'add_empty' => false)),
      'project_id'   => new sfWidgetFormDoctrineChoice(array('model' => 'Project', 'add_empty' => false)),
      'description'  => new sfWidgetFormInput(),
      'created_date' => new sfWidgetFormDateTime(),
      'updated_date' => new sfWidgetFormDateTime(),
      'longtitude'   => new sfWidgetFormInput(),
      'latitude'     => new sfWidgetFormInput(),
    ));

    $this->setValidators(array(
      'id'           => new sfValidatorDoctrineChoice(array('model' => 'find', 'column' => 'id', 'required' => false)),
      'device_id'    => new sfValidatorDoctrineChoice(array('model' => 'Device')),
      'project_id'   => new sfValidatorDoctrineChoice(array('model' => 'Project')),
      'description'  => new sfValidatorPass(),
      'created_date' => new sfValidatorDateTime(),
      'updated_date' => new sfValidatorDateTime(),
      'longtitude'   => new sfValidatorPass(array('required' => false)),
      'latitude'     => new sfValidatorPass(array('required' => false)),
    ));

    $this->widgetSchema->setNameFormat('find[%s]');

    $this->errorSchema = new sfValidatorErrorSchema($this->validatorSchema);

    parent::setup();
  }

  public function getModelName()
  {
    return 'find';
  }

}
