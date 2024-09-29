#!/bin/bash

# 定义要运行的命令列表
COMMANDS=(
    "bazel run --jvmopt=-Xms10g --jvmopt=-Xmx30g //projects/allinone:allinone_main -- -runmode batch -cmdfile /home/liuxz/MEV/batfish-master/command/eBGP-fattree14"
    "bazel run --jvmopt=-Xms10g --jvmopt=-Xmx30g //projects/allinone:allinone_main -- -runmode batch -cmdfile /home/liuxz/MEV/batfish-master/command/eBGP-fattree16"
    "bazel run --jvmopt=-Xms10g --jvmopt=-Xmx30g //projects/allinone:allinone_main -- -runmode batch -cmdfile /home/liuxz/MEV/batfish-master/command/iBGP-full-bics"
    "bazel run --jvmopt=-Xms10g --jvmopt=-Xmx30g //projects/allinone:allinone_main -- -runmode batch -cmdfile /home/liuxz/MEV/batfish-master/command/iBGP-full-columbus"
    "bazel run --jvmopt=-Xms10g --jvmopt=-Xmx30g //projects/allinone:allinone_main -- -runmode batch -cmdfile /home/liuxz/MEV/batfish-master/command/iBGP-full-uscarrier"
    "bazel run --jvmopt=-Xms10g --jvmopt=-Xmx30g //projects/allinone:allinone_main -- -runmode batch -cmdfile /home/liuxz/MEV/batfish-master/command/spine-leaf6"
    "bazel run --jvmopt=-Xms10g --jvmopt=-Xmx30g //projects/allinone:allinone_main -- -runmode batch -cmdfile /home/liuxz/MEV/batfish-master/command/spine-leaf10"
    "bazel run --jvmopt=-Xms10g --jvmopt=-Xmx30g //projects/allinone:allinone_main -- -runmode batch -cmdfile /home/liuxz/MEV/batfish-master/command/spine-leaf20"
    "bazel run --jvmopt=-Xms10g --jvmopt=-Xmx30g //projects/allinone:allinone_main -- -runmode batch -cmdfile /home/liuxz/MEV/batfish-master/command/spine-leaf50"
    "bazel run --jvmopt=-Xms10g --jvmopt=-Xmx30g //projects/allinone:allinone_main -- -runmode batch -cmdfile /home/liuxz/MEV/batfish-master/command/spine-leaf100"
    "bazel run --jvmopt=-Xms10g --jvmopt=-Xmx30g //projects/allinone:allinone_main -- -runmode batch -cmdfile /home/liuxz/MEV/batfish-master/command/spine-leaf200"
    "bazel run --jvmopt=-Xms10g --jvmopt=-Xmx30g //projects/allinone:allinone_main -- -runmode batch -cmdfile /home/liuxz/MEV/batfish-master/command/spine-leaf500"
    "bazel run --jvmopt=-Xms10g --jvmopt=-Xmx30g //projects/allinone:allinone_main -- -runmode batch -cmdfile /home/liuxz/MEV/batfish-master/command/eBGP-fattree18"
    "bazel run --jvmopt=-Xms10g --jvmopt=-Xmx30g //projects/allinone:allinone_main -- -runmode batch -cmdfile /home/liuxz/MEV/batfish-master/command/eBGP-fattree20"

  # 在这里添加更多命令
  # "bazel run --jvmopt=-Xmx2g //projects/other:other_main -- -runmode batch -cmdfile /path/to/otherCommand"
)

# 定义对应的输出文件列表
OUTPUT_FILES=(
  "/home/liuxz/MEV/batfish-master/output/fattree14.txt"
  "/home/liuxz/MEV/batfish-master/output/fattree16.txt"
  "/home/liuxz/MEV/batfish-master/output/iBGP-full-bics.txt"
  "/home/liuxz/MEV/batfish-master/output/iBGP-full-columbus.txt"
  "/home/liuxz/MEV/batfish-master/output/iBGP-full-uscarrier.txt"
  "/home/liuxz/MEV/batfish-master/output/spine-leaf6.txt"
  "/home/liuxz/MEV/batfish-master/output/spine-leaf10.txt"
  "/home/liuxz/MEV/batfish-master/output/spine-leaf20.txt"
  "/home/liuxz/MEV/batfish-master/output/spine-leaf50.txt"
  "/home/liuxz/MEV/batfish-master/output/spine-leaf100.txt"
  "/home/liuxz/MEV/batfish-master/output/spine-leaf200.txt"
  "/home/liuxz/MEV/batfish-master/output/spine-leaf500.txt"
  "/home/liuxz/MEV/batfish-master/output/fattree18.txt"
   "/home/liuxz/MEV/batfish-master/output/fattree20.txt"


  # 在这里添加更多文件路径
  # "/path/to/output2.txt"
)

# 检查两个列表的长度是否匹配
if [ ${#COMMANDS[@]} -ne ${#OUTPUT_FILES[@]} ]; then
  echo "Error: Number of commands does not match number of output files."
  exit 1
fi

# 运行每个命令并将输出写入对应的文件
for ((i = 0; i < ${#COMMANDS[@]}; i++)); do
  CMD="${COMMANDS[$i]}"
  OUTPUT_FILE="${OUTPUT_FILES[$i]}"

  # 创建输出文件（如果不存在）
  touch "$OUTPUT_FILE"

  echo "Running command: $CMD" | tee -a "$OUTPUT_FILE"
  
  # 运行命令并将输出写入指定文件
  eval "$CMD" > "$OUTPUT_FILE" 2>&1
  
  if [ $? -eq 0 ]; then
    echo "Command $i completed successfully." | tee -a "$OUTPUT_FILE"
  else
    echo "Command $i failed. Check the output for details." | tee -a "$OUTPUT_FILE"
  fi
  
  # 添加分隔符以区分不同命令的输出
  echo "---------------------------------------" | tee -a "$OUTPUT_FILE"
done
