package com.sky.service;

import com.sky.annotation.AutoFill;
import com.sky.dto.EmployeeDTO;
import com.sky.dto.EmployeeLoginDTO;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.entity.Employee;
import com.sky.enumeration.OperationType;
import com.sky.result.PageResult;
import org.apache.ibatis.annotations.Insert;

public interface EmployeeService {

    /**
     * 员工登录
     * @param employeeLoginDTO
     * @return
     */
    Employee login(EmployeeLoginDTO employeeLoginDTO);
    @AutoFill(value = OperationType.INSERT)
    void save(EmployeeDTO employeeDTO);
    //PageResult pageResult =  employeeService.pageQuery(employeePageQueryDTO);
    PageResult pageQuery(EmployeePageQueryDTO employeePageQueryDTO);

    void startOrstop(Integer status, Long id);
    //根据id查询员工
    Employee getById(Long id);
    @AutoFill(value = OperationType.UPDATE)
    void update(EmployeeDTO employeeDTO);
}
