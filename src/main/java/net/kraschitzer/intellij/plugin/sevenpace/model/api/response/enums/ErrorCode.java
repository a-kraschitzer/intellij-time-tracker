package net.kraschitzer.intellij.plugin.sevenpace.model.api.response.enums;

public enum ErrorCode {

    /**
     * Unexpected error occurred.
     * General unhanded error during internal logic execution. It will contain additional info with original error and original stack trace.
     */
    UnexpectedError,

    /**
     * Unknown error occurred. See additional details for more information
     * Represents error which has occurred outside of Timetracker logic environment. Usually happens when server or cloud issues are present.
     */
    UnknownError,

    /**
     * API is disabled due account maintenance
     * When account is migrating between instances of our application API is disabled to maintain data integrity.
     */
    ApiDisabled,

    /**
     * System in a read only state (check your license)
     * For OnPremises (TFS) only. If license is not valid or expired _ API will be disabled as well as main system.
     */
    LicenseReadOnly,

    /**
     * Activity type feature is not enabled
     * Error will affect only "/activityTypes/{id}" endpoint if you try to get any specific Activity Type by Id when feature is disabled.
     */
    ActivityTypeDisabled,

    /**
     * API Version not specified, or path do not support required version, or supplied parameters has invalid values
     * Due the way our system handles versioning, it will fail with version error even if you're specifying correct version of API for endpoint, but supplied incorrect types of parameters (eg. not valid GUID). Recheck your request.
     */
    ApiVersionError,

    /**
     * Quota exceeded
     * You're making to many request. Currently quota set to 10 request per second and 100 requests per minute per user. Check header "Retry_After" for value in seconds when block will be lifted.
     */
    QuoteExceededError,

    /**
     * Unauthorized
     * You're using invalid authorization credentials or not provided any authorization info. Check here or here for authorization options.
     */
    UnauthorizedError,

    /**
     * Access denied (you don't have enough rights?)
     * You've been successfully authorized, but you don't have enough rights to execute requested resource.
     */
    AccessDenied,


    /**
     * Entity not found
     * Occurs when requested by Id entity is not present in Database.
     */
    EntityNotFound,

    /**
     * WorkItemId or Comment required
     * Occurs when creating or updating worklog. It must have either comment or be associated with Work Item.
     */
    WorkItemIdOrCommentRequired,

    /**
     * Length is required
     * Occurs when creating or updating worklog. It must have length and it cannot be automatically set.
     */
    LengthIsRequired,

    /**
     * Length must be greater than 0
     * Occurs when creating or updating worklog. It must have length as positive integer value.
     */
    LengthMustBePositive,

    /**
     * Adding worklogs in a future not allowed by settings
     * Occurs when creating or updating worklog. Managed by setting "Prevent Time Tracking Beyond Present Day". If Timestamp of a worklog is not meeting selected requirements, this error is raised.
     */
    FutureCheckFailed,

    /**
     * Selected date is locked for editing by approval
     * Occurs when creating, updating or deleting worklog. If date of worklog is within approved interval, editing is restricted.
     */
    ApprovalStateFailed,

    /**
     * Worklog cannot be modified by you
     * Occurs when creating, updating or deleting worklog. If worklog doesn't belong to you and (when deleting) you don't have rights to delete it, error is raised.
     */
    NoRightsToWorkLogModification,

    /**
     * Filter work items ids count exceeding maximum allowed value (100)
     * Occurs when requesting list of worklogs and using filter "$workItemIds". If amount of Ids is exceeding maximum allowed value (100), error is raised.
     */
    FilterWorkItemIdsCountExceeded,

    /**
     * Error while committing entity to database
     * Occurs when creating, updating or deleting worklog. General error if entity is failing validation in Database. Will contain additional information about errors.
     */
    DbValidationError,

    ;
}
