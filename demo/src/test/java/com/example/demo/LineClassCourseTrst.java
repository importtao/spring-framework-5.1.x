
package com.example.demo;

import com.planet.service.UserService;
import com.planet.service.kidHour.KidHourCommonService;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;


/**
 * @program planet
 * @description: 混排单元测试
 * @author: changhu
 * @create: 2019/04/06 16:57
 */

public class LineClassCourseTrst extends BaseTest {
    @Autowired
    UserService userService;
    @Autowired
    KidHourCommonService kidHourCommonService;


    @Test
    public void classCourseTeacher() {
        kidHourCommonService.classCourseTeacher();
    }
}

