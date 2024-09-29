#!/bin/bash

# ����Ҫ���е������б�
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

  # ��������Ӹ�������
  # "bazel run --jvmopt=-Xmx2g //projects/other:other_main -- -runmode batch -cmdfile /path/to/otherCommand"
)

# �����Ӧ������ļ��б�
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


  # ��������Ӹ����ļ�·��
  # "/path/to/output2.txt"
)

# ��������б�ĳ����Ƿ�ƥ��
if [ ${#COMMANDS[@]} -ne ${#OUTPUT_FILES[@]} ]; then
  echo "Error: Number of commands does not match number of output files."
  exit 1
fi

# ����ÿ����������д���Ӧ���ļ�
for ((i = 0; i < ${#COMMANDS[@]}; i++)); do
  CMD="${COMMANDS[$i]}"
  OUTPUT_FILE="${OUTPUT_FILES[$i]}"

  # ��������ļ�����������ڣ�
  touch "$OUTPUT_FILE"

  echo "Running command: $CMD" | tee -a "$OUTPUT_FILE"
  
  # ������������д��ָ���ļ�
  eval "$CMD" > "$OUTPUT_FILE" 2>&1
  
  if [ $? -eq 0 ]; then
    echo "Command $i completed successfully." | tee -a "$OUTPUT_FILE"
  else
    echo "Command $i failed. Check the output for details." | tee -a "$OUTPUT_FILE"
  fi
  
  # ��ӷָ��������ֲ�ͬ��������
  echo "---------------------------------------" | tee -a "$OUTPUT_FILE"
done
