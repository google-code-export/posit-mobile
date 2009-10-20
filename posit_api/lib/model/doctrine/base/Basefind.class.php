<?php

/**
 * This class has been auto-generated by the Doctrine ORM Framework
 */
abstract class Basefind extends sfDoctrineRecord
{
    public function setTableDefinition()
    {
        $this->setTableName('find');
        $this->hasColumn('device_id', 'integer', null, array('type' => 'integer', 'notnull' => true));
        $this->hasColumn('project_id', 'integer', null, array('type' => 'integer', 'notnull' => true));
        $this->hasColumn('description', 'object', null, array('type' => 'object', 'notnull' => true));
        $this->hasColumn('created_date', 'timestamp', null, array('type' => 'timestamp', 'notnull' => true));
        $this->hasColumn('updated_date', 'timestamp', null, array('type' => 'timestamp', 'notnull' => true));
        $this->hasColumn('longtitude', 'double', null, array('type' => 'double'));
        $this->hasColumn('latitude', 'double', null, array('type' => 'double'));

        $this->option('type', 'MyISAM');
        $this->option('collate', 'utf8_unicode_ci');
        $this->option('charset', 'utf8');
    }

    public function setUp()
    {
        $this->hasMany('Project', array('local' => 'project_id',
                                        'foreign' => 'id'));

        $this->hasMany('Device', array('local' => 'device_id',
                                       'foreign' => 'id'));
    }
}