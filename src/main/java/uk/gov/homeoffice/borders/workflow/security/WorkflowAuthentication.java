package uk.gov.homeoffice.borders.workflow.security;

import org.camunda.bpm.engine.impl.identity.Authentication;
import uk.gov.homeoffice.borders.workflow.identity.Team;
import uk.gov.homeoffice.borders.workflow.identity.ShiftUser;

import java.util.ArrayList;
import java.util.List;

import static java.util.stream.Collectors.toList;

public class WorkflowAuthentication extends Authentication {

    private ShiftUser user;

    public WorkflowAuthentication() {
        super();
    }

    public WorkflowAuthentication(String authenticatedUserId, List<String> groupIds) {
        super(authenticatedUserId, groupIds, null);
    }

    public WorkflowAuthentication(String authenticatedUserId, List<String> authenticatedGroupIds, List<String> authenticatedTenantIds) {
        super(authenticatedUserId, authenticatedGroupIds, authenticatedTenantIds);
    }

    public WorkflowAuthentication(ShiftUser user) {
        super(user.getEmail(), user.getTeams().stream().map(Team::getTeamCode).collect(toList()), new ArrayList<>());
        this.user = user;
    }

    public ShiftUser getUser() {
        return this.user;
    }
}
