package org.motechproject.mds.ex.entity;

import org.motechproject.mds.ex.MdsException;

/**
 * Thrown when there were some error during combobox data migration.
 */
public class DataMigrationFailedException extends MdsException {

    private static final long serialVersionUID = 8752686601917633293L;

    public DataMigrationFailedException(String message, Throwable cause) {
        super(message, cause);
    }
}
