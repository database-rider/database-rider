package com.github.database.rider.springboot4.mapper.company;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.github.database.rider.springboot4.entity.company.Company;
import com.github.database.rider.springboot4.entity.company.CompanyExample;

@Mapper
public interface CompanyMapper {

    long countByExample(CompanyExample example);

    int deleteByExample(CompanyExample example);

    int deleteByPrimaryKey(Long id);

    int insert(Company row);

    int insertSelective(Company row);

    List<Company> selectByExample(CompanyExample example);

    Company selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("row") Company row, @Param("example") CompanyExample example);

    int updateByExample(@Param("row") Company row, @Param("example") CompanyExample example);

    int updateByPrimaryKeySelective(Company row);

    int updateByPrimaryKey(Company row);
}