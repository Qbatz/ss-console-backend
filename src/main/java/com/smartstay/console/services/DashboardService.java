package com.smartstay.console.services;

import com.smartstay.console.config.Authentication;
import com.smartstay.console.dao.*;
import com.smartstay.console.dto.dashboard.DashboardOwnerProjection;
import com.smartstay.console.dto.hostel.DashboardCityGraphProjection;
import com.smartstay.console.dto.hostel.DashboardRegionHostelProjection;
import com.smartstay.console.dto.hostel.HostelLiteProjection;
import com.smartstay.console.ennum.DashboardRegionGraphDateFilterEnum;
import com.smartstay.console.ennum.ModuleId;
import com.smartstay.console.responses.dashboard.*;
import com.smartstay.console.utils.UserActivityUtil;
import com.smartstay.console.utils.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    @Autowired
    private Authentication authentication;
    @Autowired
    private HostelService hostelService;
    @Autowired
    private OwnersService ownersService;
    @Autowired
    private AgentService agentService;
    @Autowired
    private DemoRequestService demoRequestService;
    @Autowired
    private SubscriptionService subscriptionService;
    @Autowired
    private AgentRolesService agentRolesService;
    @Autowired
    private BedsService bedsService;
    @Autowired
    private LoginHistoryService loginHistoryService;
    @Autowired
    private UsersService usersService;
    @Autowired
    private UserActivitiesService userActivitiesService;
    @Autowired
    private UserActivityUtil userActivityUtil;

    public ResponseEntity<?> getDashboard(String regionGraphDateFilter) {

        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }

        Agent agent = agentService.findUserByUserId(authentication.getName());
        if (agent == null) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }

        AgentRoles agentRole = agentRolesService.findById(agent.getRoleId());
        if (agentRole == null) {
            return new ResponseEntity<>(Utils.AGENT_ROLE_NOT_FOUND, HttpStatus.BAD_REQUEST);
        }

        DashboardRegionGraphDateFilterEnum regionGraphDateFilterEnum;
        try {
            regionGraphDateFilterEnum = DashboardRegionGraphDateFilterEnum.valueOf(regionGraphDateFilter);
        } catch (Exception e) {
            return new ResponseEntity<>("Region graph date filter not found", HttpStatus.BAD_REQUEST);
        }

        long hostelCount = 0;
        long activeHostelCount = 0;
        long ownersCount = 0;
        long agentCount = 0;
        long demoRequestCount = 0;
        long expiredSubscriptionsCount = 0;
        long paidHostelCount = 0;
        long activePaidHostelCount = 0;
        long bedCount = 0;
        long multiBranchOwnerCount = 0;
        long usedLast45DaysCount = 0;

        Set<String> activeHostelIds;

        Date today = new Date();
        Date todayStart = Utils.getStartOfDay(today);

        Date regionGraphStartDate = null;
        Date regionGraphEndDate = null;

        if (DashboardRegionGraphDateFilterEnum.LAST_7_DAYS.equals(regionGraphDateFilterEnum)) {
            regionGraphStartDate = Utils.addDaysToDate(today, -6);
            regionGraphEndDate = today;
        } else if (DashboardRegionGraphDateFilterEnum.LAST_30_DAYS.equals(regionGraphDateFilterEnum)) {
            regionGraphStartDate = Utils.addDaysToDate(today, -29);
            regionGraphEndDate = today;
        } else if (DashboardRegionGraphDateFilterEnum.THIS_MONTH.equals(regionGraphDateFilterEnum)) {
            regionGraphStartDate = Utils.getFirstDayOfMonth(today);
            regionGraphEndDate = today;
        } else if (DashboardRegionGraphDateFilterEnum.THIS_QUARTER.equals(regionGraphDateFilterEnum)) {
            regionGraphStartDate = Utils.getStartOfQuarter(today);
            regionGraphEndDate = today;
        }

        if (regionGraphStartDate != null){
            regionGraphStartDate = Utils.getStartOfDay(regionGraphStartDate);
        }
        if (regionGraphEndDate != null){
            regionGraphEndDate = Utils.getStartOfDay(
                    Utils.addDaysToDate(regionGraphEndDate, 1));
        }

        List<DashboardRegionGraphRes> dashboardRegionGraphResList = new ArrayList<>();
        List<DashboardRegionDataRes> regionData = new ArrayList<>();
        List<DashboardOwnerDataRes> ownerData = new ArrayList<>();

        if (agentRolesService.checkPermission(agentRole, ModuleId.Hostels.getId(), Utils.PERMISSION_READ)) {

            hostelCount = hostelService.getHostelCount();

            multiBranchOwnerCount = hostelService.getParentIdCountWithMultipleHostels();

            List<DashboardCityGraphProjection> topCities = hostelService
                    .getTopCitiesForDashboard(regionGraphStartDate, regionGraphEndDate);

            dashboardRegionGraphResList = topCities.stream()
                    .map(city -> new DashboardRegionGraphRes(
                            Utils.capitalizeWords(city.getCity()),
                            city.getCount()
                    )).toList();

            List<DashboardRegionHostelProjection> hostelsByCities =
                    hostelService.getHostelsFromRecentlyAddedCities();

            Map<String, List<DashboardRegionHostelProjection>> hostelsByCity =
                    hostelsByCities.stream()
                            .filter(h -> h.getCity() != null && !h.getCity().isBlank())
                            .collect(Collectors.groupingBy(
                                    h -> h.getCity().trim().toLowerCase(),
                                    LinkedHashMap::new,
                                    Collectors.toList()
                            ));

            regionData = hostelsByCity.values().stream()
                    .map(hostels -> {

                        DashboardRegionHostelProjection latestHostel = hostels.stream()
                                .max(Comparator.comparing(
                                        DashboardRegionHostelProjection::getCreatedAt
                                ))
                                .orElseThrow();

                        long ownerCount = hostels.stream()
                                .map(DashboardRegionHostelProjection::getParentId)
                                .filter(Objects::nonNull)
                                .distinct()
                                .count();

                        return new DashboardRegionDataRes(
                                Utils.capitalizeWords(latestHostel.getCity()),
                                Utils.capitalizeWords(latestHostel.getState()),
                                ownerCount,
                                hostels.size(),
                                Utils.getRelativeDateDisplay(latestHostel.getCreatedAt())
                        );
                    })
                    .toList();

            List<DashboardOwnerProjection> ownerProjections = hostelService
                    .getOwnersByHostelCount();

            Set<String> parentIds = ownerProjections.stream()
                    .map(DashboardOwnerProjection::getParentId)
                    .collect(Collectors.toSet());

            List<Users> owners = usersService
                    .getOwners(new ArrayList<>(parentIds));

            Map<String, Users> ownersMap = owners.stream()
                    .collect(Collectors.toMap(Users::getParentId,
                            Function.identity(), (a,b) -> a));

            ownerData = ownerProjections.stream()
                    .map(i -> {
                        Users owner = ownersMap.getOrDefault(i.getParentId(), null);

                        String ownerId = null;
                        String ownerName = null;
                        if (owner != null){
                            ownerId = owner.getUserId();
                            ownerName = Utils.getFullName(owner.getFirstName(), owner.getLastName());
                        }

                        return new DashboardOwnerDataRes(ownerId, ownerName,
                                i.getHostelCount(), i.getCityCount());
                    }).toList();
        }

        if (agentRolesService.checkPermission(agentRole, ModuleId.Owners.getId(), Utils.PERMISSION_READ)) {

            ownersCount = ownersService.getOwnerCount();

            Set<String> targetParentIds = hostelService.getActiveParentIds();

            List<HostelLiteProjection> allHostels = hostelService
                    .getHostelsLiteProjectionByParentIds(targetParentIds);

            Map<String, Date> parentCreatedDateMap = allHostels.stream()
                    .collect(Collectors.toMap(
                            HostelLiteProjection::getParentId,
                            HostelLiteProjection::getCreatedAt,
                            BinaryOperator.minBy(Date::compareTo)));

            List<LoginHistory> latestLogins = loginHistoryService
                    .getLoginHistoriesByParentIds(new ArrayList<>(targetParentIds));

            for (LoginHistory login : latestLogins) {

                Date loginAtStart = Utils.getStartOfDay(login.getLoginAt());
                Date parentCreatedDate = Utils.getStartOfDay(
                        parentCreatedDateMap.get(login.getParentId()));

                if (loginAtStart == null || parentCreatedDate == null) {
                    continue;
                }

                if (parentCreatedDate.equals(loginAtStart)) {
                    continue;
                }

                long days = Utils.daysBetween(loginAtStart, todayStart);

                if (days <= 45) {
                    usedLast45DaysCount++;
                }
            }
        }

        if (agentRolesService.checkPermission(agentRole, ModuleId.Agents.getId(), Utils.PERMISSION_READ)) {

            agentCount = agentService.getAgentCount();
        }

        demoRequestCount = demoRequestService.getDemoRequestCount();

        if (agentRolesService.checkPermission(agentRole, ModuleId.Subscriptions.getId(), Utils.PERMISSION_READ)) {

            activeHostelIds = hostelService.getActiveHostelIds();

            if (activeHostelIds != null) {
                expiredSubscriptionsCount = subscriptionService
                        .getExpiredSubscriptionsCountByHostelIds(activeHostelIds);

                activeHostelCount = activeHostelIds.size() - expiredSubscriptionsCount;

                Set<String> paidHostelIds = subscriptionService
                        .getHostelIdsWithPaidSubscriptions(activeHostelIds);

                long expiredPaidSubscriptionsCount = subscriptionService
                        .getExpiredSubscriptionsCountByHostelIds(paidHostelIds);

                paidHostelCount = paidHostelIds != null ? paidHostelIds.size() : 0;
                activePaidHostelCount = paidHostelCount - expiredPaidSubscriptionsCount;
            }
        }

        bedCount = bedsService.getBedCount();

        List<UserActivities> userActivities = userActivitiesService
                .getLimitedRecentUserActivities(5);

        Set<String> hostelIds = userActivities.stream()
                .map(UserActivities::getHostelId)
                .collect(Collectors.toSet());

        List<HostelV1> hostels = hostelService
                .getHostelsByHostelIds(hostelIds);

        Map<String, HostelV1> hostelMap = hostels.stream()
                .collect(Collectors.toMap(HostelV1::getHostelId,
                        Function.identity(), (a,b) -> a));

        List<DashboardUserActivitiesRes> dashboardUserActivitiesRes = userActivities.stream()
                .map(i -> {

                    HostelV1 hostel = hostelMap.getOrDefault(i.getHostelId(), null);

                    String hostelName = null;
                    String hostelInitials = null;
                    String hostelMainImage = null;
                    String hostelAddress = null;
                    String hostelCity = null;

                    if (hostel != null) {
                        hostelName = hostel.getHostelName();
                        hostelInitials = Utils.getInitials(hostelName);
                        hostelMainImage = hostel.getMainImage();
                        hostelAddress = Utils.buildFullAddress(hostel);
                        hostelCity = hostel.getCity();
                    }

                    return new DashboardUserActivitiesRes(userActivityUtil.getDescription(i), i.getActivityType(),
                            i.getSource(), i.getHostelId(), hostelName, hostelInitials, hostelMainImage,
                            hostelAddress, hostelCity, Utils.getRelativeTimeDisplay(i.getCreatedAt()));
                }).toList();

        List<DashboardRegionGraphDateFilterRes> regionGraphDateFilterRes = Arrays
                .stream(DashboardRegionGraphDateFilterEnum.values())
                .map(i -> new DashboardRegionGraphDateFilterRes(
                        i.name(), i.getValue()
                )).toList();

        DashboardResponse response = new DashboardResponse(hostelCount, activeHostelCount, ownersCount,
                agentCount, demoRequestCount, expiredSubscriptionsCount, bedCount, paidHostelCount,
                activePaidHostelCount, multiBranchOwnerCount, usedLast45DaysCount, regionGraphDateFilterRes,
                dashboardRegionGraphResList, regionData, ownerData, dashboardUserActivitiesRes);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
