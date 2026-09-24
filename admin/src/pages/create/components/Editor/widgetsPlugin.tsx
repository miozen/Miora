import type { BytemdPlugin } from 'bytemd';
import { Modal, Form, Input, message } from 'antd';

import widgetSvg from './icon/widget.svg?raw';

type InsertHandler = (block: string) => void;

function wrapWidget(payload: Record<string, unknown>) {
  return `\`\`\`tx-widget\n${JSON.stringify(payload, null, 2)}\n\`\`\``;
}

function openSimpleModal(
  title: string,
  fields: Array<{ name: string; label: string; required?: boolean; placeholder?: string }>,
  toPayload: (values: Record<string, string>) => Record<string, unknown>,
  appendBlock: InsertHandler,
) {
  let values: Record<string, string> = {};

  Modal.confirm({
    title,
    width: 480,
    icon: null,
    content: (
      <Form layout="vertical" className="mt-3" onValuesChange={(_, all) => { values = all; }}>
        {fields.map((field) => (
          <Form.Item
            key={field.name}
            name={field.name}
            label={field.label}
            rules={field.required === false ? undefined : [{ required: true, message: `请填写${field.label}` }]}
          >
            <Input placeholder={field.placeholder} />
          </Form.Item>
        ))}
      </Form>
    ),
    okText: '插入',
    cancelText: '取消',
    onOk: () => {
      for (const field of fields) {
        if (field.required === false) continue;
        if (!values[field.name]?.trim()) {
          message.error(`请填写${field.label}`);
          return Promise.reject();
        }
      }
      appendBlock(wrapWidget(toPayload(values)));
    },
  });
}

const widgetPresets: Array<{
  title: string;
  insert: (appendBlock: InsertHandler) => void;
}> = [
  {
    title: 'Bilibili',
    insert: (appendBlock) =>
      openSimpleModal(
        '插入 Bilibili',
        [{ name: 'bvid', label: 'BV 号', placeholder: 'BV1GJ411x7h7' }],
        (v) => ({ type: 'bilibili', bvid: v.bvid.trim() }),
        appendBlock,
      ),
  },
  {
    title: 'YouTube',
    insert: (appendBlock) =>
      openSimpleModal(
        '插入 YouTube',
        [{ name: 'id', label: '视频 ID', placeholder: 'dQw4w9WgXcQ' }],
        (v) => ({ type: 'youtube', id: v.id.trim() }),
        appendBlock,
      ),
  },
  {
    title: '网易云',
    insert: (appendBlock) =>
      openSimpleModal(
        '插入网易云音乐',
        [{ name: 'id', label: '歌曲 ID', placeholder: '1824045033' }],
        (v) => ({ type: 'netease', id: v.id.trim() }),
        appendBlock,
      ),
  },
  {
    title: '抖音',
    insert: (appendBlock) =>
      openSimpleModal(
        '插入抖音视频',
        [{ name: 'id', label: '视频 ID', placeholder: '7234567890123456789' }],
        (v) => ({ type: 'douyin', id: v.id.trim() }),
        appendBlock,
      ),
  },
  {
    title: '音频',
    insert: (appendBlock) =>
      openSimpleModal(
        '插入音频',
        [
          { name: 'title', label: '标题', placeholder: '音频标题' },
          { name: 'src', label: '音频地址', placeholder: 'https://...' },
        ],
        (v) => ({ type: 'audio', title: v.title.trim(), src: v.src.trim() }),
        appendBlock,
      ),
  },
  {
    title: 'Tabs',
    insert: (appendBlock) =>
      appendBlock(
        wrapWidget({
          type: 'tabs',
          items: [
            { title: '方案 A', content: '内容 A' },
            { title: '方案 B', content: '内容 B' },
          ],
        }),
      ),
  },
  {
    title: '时间线',
    insert: (appendBlock) =>
      appendBlock(
        wrapWidget({
          type: 'timeline',
          items: [
            { time: '2026-01', title: '开始', content: '描述' },
            { time: '2026-07', title: '完成', content: '描述' },
          ],
        }),
      ),
  },
  {
    title: '步骤条',
    insert: (appendBlock) =>
      appendBlock(
        wrapWidget({
          type: 'steps',
          items: [
            { title: '第一步', content: '说明' },
            { title: '第二步', content: '说明' },
          ],
        }),
      ),
  },
  {
    title: '画廊',
    insert: (appendBlock) =>
      appendBlock(
        wrapWidget({
          type: 'gallery',
          items: [
            { src: 'https://picsum.photos/seed/g1/800/600', alt: '图片1' },
            { src: 'https://picsum.photos/seed/g2/800/600', alt: '图片2' },
          ],
        }),
      ),
  },
  {
    title: 'CTA',
    insert: (appendBlock) =>
      openSimpleModal(
        '插入 CTA',
        [
          { name: 'title', label: '标题' },
          { name: 'description', label: '描述', required: false },
          { name: 'primaryText', label: '按钮文案', placeholder: '了解更多' },
          { name: 'primaryUrl', label: '按钮链接', placeholder: 'https://...' },
        ],
        (v) => ({
          type: 'cta',
          title: v.title.trim(),
          description: v.description?.trim() || undefined,
          primaryText: v.primaryText.trim(),
          primaryUrl: v.primaryUrl.trim(),
        }),
        appendBlock,
      ),
  },
];

export const widgets = (): BytemdPlugin => {
  return {
    actions: [
      {
        title: '小组件',
        icon: widgetSvg,
        handler: {
          type: 'dropdown',
          actions: widgetPresets.map(({ title, insert }) => ({
            title,
            handler: {
              type: 'action',
              click: (ctx) => insert((block) => ctx.appendBlock(block)),
            },
          })),
        },
      },
    ],
  };
};
