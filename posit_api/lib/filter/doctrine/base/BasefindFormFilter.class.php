<?php

require_once(sfConfig::get('sf_lib_dir').'/filter/doctrine/BaseFormFilterDoctrine.class.php');

/**
 * find filter form base class.
 *
 * @package    filters
 * @subpackage find *
 * @version    SVN: $Id: sfDoctrineFormFilterGeneratedTemplate.php 11675 2008-09-19 15:21:38Z fabien $
 */
class BasefindFormFilter extends BaseFormFilterDoctrine
{
  public function setup()
  {
    $this->setWidgets(array(
      'device_id'    => new sfWidgetFormDoctrineChoice(array('model' => 'Device', 'add_empty' => true)),
      'project_id'   => new sfWidgetFormDoctrineChoice(array('model' => 'Project', 'add_empty' => true)),
      'description'  => new sfWidgetFormFilterInput(),
      'created_date' => new sfWidgetFormFilterDate(array('from_date' => new sfWidgetFormDate(), 'to_date' => new sfWidgetFormDate(), 'with_empty' => false)),
      'updated_date' => new sfWidgetFormFilterDate(array('from_date' => new sfWidgetFormDate(), 'to_date' => new sfWidgetFormDate(), 'with_empty' => false)),
      'longtitude'   => new sfWidgetFormFilterInput(),
      'latitude'     => new sfWidgetFormFilterInput(),
    ));

    $this->setValidators(array(
      'device_id'    => new sfValidatorDoctrineChoice(array('required' => false, 'model' => 'Device', 'column' => 'id')),
      'project_id'   => new sfValidatorDoctrineChoice(array('required' => false, 'model' => 'Project', 'column' => 'id')),
      'description'  => new sfValidatorPass(array('required' => false)),
      'created_date' => new sfValidatorDateRange(array('required' => false, 'from_date' => new sfValidatorDate(array('required' => false)), 'to_date' => new sfValidatorDate(array('required' => false)))),
      'updated_date' => new sfValidatorDateRange(array('required' => false, 'from_date' => new sfValidatorDate(array('required' => false)), 'to_date' => new sfValidatorDate(array('required' => false)))),
      'longtitude'   => new sfValidatorPass(array('required' => false)),
      'latitude'     => new sfValidatorPass(array('required' => false)),
    ));

    $this->widgetSchema->setNameFormat('find_filters[%s]');

    $this->errorSchema = new sfValidatorErrorSchema($this->validatorSchema);

    parent::setup();
  }

  public function getModelName()
  {
    return 'find';
  }

  public function getFields()
  {
    return array(
      'id'           => 'Number',
      'device_id'    => 'ForeignKey',
      'project_id'   => 'ForeignKey',
      'description'  => 'Text',
      'created_date' => 'Date',
      'updated_date' => 'Date',
      'longtitude'   => 'Text',
      'latitude'     => 'Text',
    );
  }
}