<#include "/header.ftl">
<div class="container disk">
    <div class="row title">
        <div class="row-name text-left"><strong>文件</strong></div>
        <div class="row-size text-right"><strong>大小</strong></div>
        <div class="row-last text-right"><strong>最后修改时间</strong></div>
    </div>
    
    <ul class="disk-items row">
    <#if parent?exists && parent??>
    <li class="disk-item">
        <a href="${basePath}${parent}">
            <div class="row">
                <div class="row-name text-left">返回上一级目录</div>
                <div class="row-size text-right">-</div>
                <div class="row-last text-right">-</div>
            </div>
        </a>
    </li>
    </#if>
    <#if fileList?exists && (fileList?size>0)>
    <#list fileList as row>
    <li class="disk-item">
        <a class="" href="${basePath}${row.path}">
            <div class="row">
                <div class="row-name text-left">${row.name}</div>
                <div class="row-size text-right">${row.is_folder?string("-", row.size)}</div>
                <div class="row-last text-right">${row.is_folder?string("-", row.last_modified)}</div>
            </div>
        </a>
    </li>
    </#list>
    <#else>
    <div class="row">
        <div class="col-md-12 text-center">当前目录下没有文件</div>
    </div>
    </#if>
    </ul>
</div>
<#include "/footer.ftl">