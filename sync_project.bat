@echo off
echo ===============================================
echo   正在自动同步 [前端 + 后端] 到 GitHub...
echo ===============================================

:: 1. 进入根目录（确保路径正确）
cd /d %~dp0

:: 2. 添加所有变动（包含前后端文件夹内的所有代码）
git add .

:: 3. 提交更改，自动附带当前时间
set commit_msg="Update: %date% %time%"
git commit -m %commit_msg%

:: 4. 强制确保本地分支名为 main（防止 master 冲突）
git branch -M main

:: 5. 推送到远程仓库
echo 正在上传代码...
git push origin main

echo.
echo ===============================================
echo   同步成功！你可以刷新 GitHub 页面查看了。
echo ===============================================
pause